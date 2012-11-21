package org.eclipse.imp.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.imp.preferences.PreferencesService.ConstantEvaluator;
import org.eclipse.imp.preferences.PreferencesService.ParamEvaluator;

/**
 * This class contains a parser for strings such as:
 * 
 *  -- "foobar${pluginLoc:lpg.runtime}bletchbletch"
 *  -- "abcdef"
 *  -- "${pluginResource:lpg.runtime/lpgexe/lpg-${os}_${arch}}"
 *  
 * as well as the small AST hierarchy to represent that results of parsing and 
 * subsititute the variables and parametrized variables they represent.
 */
public class PreferenceValues {
  private int cursor = 0;
  private final static char[] escapable;
  
  static { 
    escapable = new char[] { ':' , '{' , ' ' , '}' , '$', '\\' };
  
      // sorting is needed since we use Arrays.binarySearch later
      Arrays.sort(escapable);
  }
  
  private char[] input;

  public Value parse(String input) {
    try {
      this.input = input.toCharArray();
      this.cursor = 0;
      return parseComposite();
    }
    finally {
      // this is to clean up the working state of the parser in case a parse error is thrown
      this.input = null;
      this.cursor = -1;
    }
  }
  
  private char peek() {
    if (cursor < input.length) {
      return input[cursor];
    }
    else {
      return '\0';
    }
  }
  
  private char next() {
    if (cursor < input.length) {
      return input[cursor++];
    }
    else {
      return '\0';
    }
  }
  
  private Value parseComposite() {
    char ch = peek();
    List<Value> elements = new LinkedList<Value>();
    
    while (ch != '}' && cursor < input.length) {
      switch (ch = next()) {
      case '}': 
        break;
      case '$':
        if (peek() == '{') {
          elements.add(parseVariable());
        }
        else {
          return new Error("expected \'{\', but got " + peek() + " at offset " + cursor);
        }
        break;
      case '\\':
        elements.add(parseEscape());
        break;
      default:
        elements.add(new Terminal(Character.toString(ch)));
      }
    }
    
    return new Composite(elements);
  }

  private Value parseEscape() {
    char ch = next();
    
    if (Character.isLetterOrDigit(ch) || Arrays.binarySearch(escapable, ch) >= 0) {
      return new Terminal(Character.toString(ch));
    }
    else {
      return new Error("unknown escape \\" + ch + " at " + cursor + " in \"" + Arrays.toString(input) + "\"");
    }
  }

  private Value parseVariable() {
    StringBuilder name = new StringBuilder();
    char ch = next();
    
    if (ch == '{') {
      for (ch = next();ch != ':' && ch != '}' && cursor < input.length; ch = next()) {
        name.append(ch);
      }
      
      if (ch == ':') {
        return new ParameterizedVariable(name.toString(), parseComposite());
      }
      else if (ch == '}') {
        if (name.length() == 0) {
          return new Error("empty variable name is not allowed at " + cursor);
        }
        return new Variable(name.toString());
      }
      else {
        return new Error("expected } but got " + ch + " at " + cursor);
      }
    }
    else {
      return new Error("expected { but got " + ch + " at " + cursor);
    }
  }

  public interface Value {
    public String substitute(Map<String, ParamEvaluator> params, Map<String, ConstantEvaluator> constants);  
  }
  
  
  public static class Composite implements Value {
    private final List<Value> elements;

    public Composite(List<Value> elements) {
      this.elements = Collections.unmodifiableList(elements);
    }
    
    public String substitute(Map<String, ParamEvaluator> params, Map<String, ConstantEvaluator> constants) {
      StringBuilder n = new StringBuilder();
      for (Value e : elements) {
        n.append(e.substitute(params, constants));
      }
      return n.toString();
    }
  }
  
  public static class Terminal implements Value {
    private final String value;

    public Terminal(String value) {
      this.value = value;
    }
    
    public String substitute(Map<String, ParamEvaluator> params, Map<String, ConstantEvaluator> constants) {
      return value;
    }
  }    
  
  /**
   * We safe parse errors in this AST class, which is a design flaw, but at least
   * it is backward compatible with the previous implementation of the preference values.
   */
  public static class Error implements Value {
    private final String message;
    
    public Error(String message) {
      this.message = message;
    }
    
    public String substitute(Map<String, ParamEvaluator> params, Map<String, ConstantEvaluator> constants) {
      return "Invalid preference: " + message;
    }
  }
  
  public static class Variable implements Value {
    private final String name;
    
    public Variable(String name) {
      this.name = name;
    }
    
    public String substitute(Map<String, ParamEvaluator> params, Map<String, ConstantEvaluator> constants) {
      if (constants.containsKey(name)) {
        return constants.get(name).getValue();
      }
      else {
        return "${" + name + "}";
      }
    }
  }
  
  public static class ParameterizedVariable implements Value {
    private final String name;
    private final Value arg;
    
    public ParameterizedVariable(String name, Value arg) {
      this.name = name;
      this.arg = arg;
    }
    
    public String substitute(Map<String, ParamEvaluator> params, Map<String, ConstantEvaluator> constants) {
      if (params.containsKey(name)) {
        return params.get(name).getValue(arg.substitute(params, constants));
      }
      else {
        return "${" + name + ":" + arg.substitute(params, constants) + "}";
      }
    }
  }
}
