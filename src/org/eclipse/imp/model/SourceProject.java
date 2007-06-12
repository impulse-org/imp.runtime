/*
 * Created on Mar 13, 2007
 */
package org.eclipse.uide.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.model.IPathEntry.PathEntryType;
import org.eclipse.uide.model.ModelFactory.ModelException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class SourceProject implements ISourceProject {
    private static final String CONFIG_FILE_NAME= ".projectConfig";
    private final IProject fProject;
    private final List<IPathEntry> fBuildPath= new ArrayList<IPathEntry>();

    SourceProject(IProject project) {
        fProject= project;

        if (!readMetaData()) {
            initializeFromJavaProject();
        }
    }

    private void initializeFromJavaProject() {
	IJavaProject javaProject= JavaCore.create(fProject);
	if (javaProject.exists()) {
	    try {
		IClasspathEntry[] cpEntries= javaProject.getResolvedClasspath(true);
		for(int i= 0; i < cpEntries.length; i++) {
		    IClasspathEntry entry= cpEntries[i];
		    IPathEntry.PathEntryType type;
		    IPath path= entry.getPath();

		    switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_CONTAINER:
			    type= PathEntryType.CONTAINER;
			    break;
			case IClasspathEntry.CPE_LIBRARY:
			    type= PathEntryType.ARCHIVE;
			    break;
			case IClasspathEntry.CPE_PROJECT:
			    type= PathEntryType.PROJECT;
			    break;
			case IClasspathEntry.CPE_SOURCE:
			    type= PathEntryType.SOURCE_FOLDER;
			    break;
			default:
//                      case IClasspathEntry.CPE_VARIABLE:
			    throw new IllegalArgumentException("Encountered variable class-path entry: " + entry.getPath().toPortableString());
		}
		    PathEntry pathEntry= new PathEntry(type, path);
		    fBuildPath.add(pathEntry);
		}
	    } catch(JavaModelException e) {
		ErrorHandler.reportError(e.getMessage(), e);
	    }
	}
    }

    // Presumably this will get called by the "New Project" wizard and the Project Properties pages
    public void commit(IProgressMonitor monitor) {
	saveMetaData(monitor);
    }

    private void saveMetaData(IProgressMonitor monitor) {
	IFile file= fProject.getFile(CONFIG_FILE_NAME);
	String metaData= imageMetaData();
	StringBufferInputStream is= new StringBufferInputStream(metaData);
	try {
	    file.setContents(is, true, true, monitor);
	} catch (CoreException e) {
	    ErrorHandler.reportError(e.getMessage(), e);
	}
    }

    private String imageMetaData() {
	StringBuffer buff= new StringBuffer();
	buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	buff.append("<project>\n");
	buff.append("  <searchPath>\n");
	for(IPathEntry path : fBuildPath) {
	    buff.append("    <pathEntry type=\"" + path.getEntryType() + "\"\n");
	    buff.append("               loc=\"" + path.getPath().toPortableString() + "\"/>\n");
	}
	buff.append("  </searchPath>\n");
	buff.append("</project>\n");
	return buff.toString();
    }

    private boolean readMetaData() {
	IFile file= fProject.getFile(CONFIG_FILE_NAME);
	if (file.exists()) {
	    try {
		InputStream contents= file.getContents();
		DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
		DocumentBuilder builder= factory.newDocumentBuilder();
		Document document= builder.parse(contents);
		NodeList children= document.getChildNodes();
		Node project= children.item(0);

		String projLabel= project.getNodeName();
		NodeList configItems= project.getChildNodes();

		for(int i=0; i < configItems.getLength(); i++) {
		    Node configItem= configItems.item(i);
		    String itemName= configItem.getNodeName();

		    if (itemName.equals("searchPath")) {
			NodeList pathItems= configItem.getChildNodes();

			for(int p= 0; p < pathItems.getLength(); p++) {
			    Node pathItem= pathItems.item(p);
			    String nodeName= pathItem.getNodeName();

			    if (nodeName.equals("#text"))
				continue;
			    if (!nodeName.equals("pathEntry")) {
				ErrorHandler.reportError("Unexpected child " + nodeName + " of <searchPath> in " + fProject.getFullPath().toPortableString() + "/" + CONFIG_FILE_NAME);
				continue;
			    }

			    NamedNodeMap attribs= pathItem.getAttributes();
			    Node typeNode= attribs.getNamedItem("type");
			    Node locNode= attribs.getNamedItem("loc");

			    if (typeNode == null || locNode == null) {
				ErrorHandler.reportError("<pathEntry> missing type or location in " + fProject.getFullPath().toPortableString() + "/" + CONFIG_FILE_NAME);
				continue;
			    }
			    String pathType= typeNode.getNodeValue();
			    String pathLoc= locNode.getNodeValue();
			    IPathEntry entry= new PathEntry(PathEntryType.valueOf(pathType), new Path(pathLoc));

			    fBuildPath.add(entry);
			}
		    } else if (itemName.equals("#text")) {
			// skip over these; should be empty anyway
		    } else
			ErrorHandler.reportError("Unrecognized project configuration item: " + itemName);
		}
		return true;
	    } catch (CoreException e) {
		ErrorHandler.reportError(e.getMessage(), e);
	    } catch (ParserConfigurationException e) {
		ErrorHandler.reportError(e.getMessage(), e);
	    } catch (SAXException e) {
		ErrorHandler.reportError(e.getMessage(), e);
	    } catch (IOException e) {
		ErrorHandler.reportError(e.getMessage(), e);
	    }
	}
	return false;
    }

    public IPath resolvePath(IPath path) {
	List<IPathEntry> buildPath= getBuildPath();
	IProject rawProject= fProject;

	if (path.isAbsolute()) {
	    return path;
	}
	IFile projRelFile= rawProject.getFile(path);

	if (projRelFile.exists())
	    return rawProject.getFullPath().append(path);

	for(IPathEntry pathEntry : buildPath) {
	    IPath entryPath= pathEntry.getPath();
	    IPathEntry.PathEntryType type= pathEntry.getEntryType();

	    if (type == IPathEntry.PathEntryType.SOURCE_FOLDER) {
		if (!entryPath.isAbsolute()) {
		    IPath filePath= entryPath.removeFirstSegments(1).append(path);
		    IFile file= rawProject.getFile(filePath);
		    if (file.exists()) {
			return filePath;
		    }
		} else {
		    // This is a pseudo source folder, not really contained within the project.
		    // The path points outside the workspace, and an IFile can't live outside the
		    // workspace. So use an ordinary Java File to determine whether this path is
		    // resolved.
		    IPath filePath= entryPath.append(path);
		    File f= new File(filePath.toOSString());
		    if (f.exists())
			return filePath;
		}
	    } else if (type == IPathEntry.PathEntryType.PROJECT) {
		try {
		    IProject defProject= ResourcesPlugin.getWorkspace().getRoot().getProject(entryPath.toPortableString());
		    ISourceProject defSrcProject= ModelFactory.open(defProject);
		    IPath defPath= scanSourceFoldersFor(defSrcProject, path);

		    if (defPath != null)
			return defPath;
		} catch (ModelException e) {
		    ErrorHandler.reportError(e.getMessage());
		}
	    } else if (type == IPathEntry.PathEntryType.ARCHIVE) {
		// ???
	    }
	}
	return null;
    }

    private IPath scanSourceFoldersFor(ISourceProject srcProject, IPath path) {
	List<IPathEntry> buildPath= srcProject.getBuildPath();
	IProject rawProject= srcProject.getRawProject();

	for(IPathEntry pathEntry : buildPath) {
	    IPath entryPath= pathEntry.getPath();
	    IPathEntry.PathEntryType type= pathEntry.getEntryType();

	    if (type == IPathEntry.PathEntryType.SOURCE_FOLDER) {
		entryPath= entryPath.removeFirstSegments(1); // make project-relative
		IPath filePath= entryPath.append(path);
		IFile f= rawProject.getFile(filePath);

		if (f.exists()) {
		    return filePath;
		}
	    }
	}
	return null;
    }

    public List<IPathEntry> getBuildPath() {
        return fBuildPath;
    }

    public IProject getRawProject() {
        return fProject.getProject();
    }

    public String toString() {
	return "<project: " + fProject.getName() + ">";
    }
}
