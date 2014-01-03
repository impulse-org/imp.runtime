imp.runtime
===========

IMP radically simplifies and speeds up the IDE development process in Eclipse, for both language with existing 
front-ends as well as languages generated using compiler and interpreter generation frameworks.

The IMP run.time is essentially a wrapper for Eclipse RCP and some JDT API that let's you focus on your language features 
as opposed to UI features. The core of the run-time is the UniversalEditor class which is a language parametric editor. 
By instantiating the UniversalEditor, using extension points, with information about your own language you can incrementally 
create a basic IDE. You provide this information mainly by implementing a number of simple interfaces.

IMP is described in a 2009 OOPSLA [paper](http://dl.acm.org/citation.cfm?id=1640104)

# Extension points 

The first and only absolutely necessary extension point to bind is "languageDescription". It makes your language known to
Eclipse and triggers a special editor whenever a file with a particular extension is opened:

```
<extension point="org.eclipse.imp.runtime.languageDescription">
      <language description="Hello" extensions=".ext" language="MyLanguage">
      </language>
   </extension>
``` 

The second most important extension point is "parserWrapper":

```
 <extension id="rascal_eclipse.parserWrapper" name="My Language parser" point="org.eclipse.imp.runtime.parser">
      <parser class="org.mylanguage.MyParseController" language="MyLanguage">
      </parser>
   </extension>
```

This will force you to implement `IParseController`, a simple interface which allows you to call your own parser and
return any kind of parse tree, abstract syntax tree or list of tokens. The point of IParseController is to be agnostic in 
what parsing technology you use or what kind of representation you have for the output of the parser. Hence the `parse` method
in IParseController returns an object of type `java.lang.Object`. Downstream this same object will be passed to you again,
for example when you want to add some syntax highlighting.

For now, my time is up describing imp.runtime, but you may have gotten the gist of this now :-) Simply search for "imp" in 
the editor of plugin.xml to discover more of the extension points of UniversalEditor. It includes features for outline, 
highlighting, menu options, etc.

IMP is not perfect, but it gives you a head start in developing an IDE without too much fuss, and it does not assume
much about the way you want to deal with your own language, except of course that you have a JVM :-)



