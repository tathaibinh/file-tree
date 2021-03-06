package com.exadel.filetree.web;

import com.exadel.filetree.data.ChangeDescription;
import com.exadel.filetree.data.FileIndex;
import com.exadel.filetree.service.IService;
import com.exadel.filetree.service.ServiceException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Set;
import java.util.TreeSet;

//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;

//import javax.servlet.annotation.WebServlet;

/**
 * Created with IntelliJ IDEA.
 * User: naXa!
 * Date: 27.06.13
 * Time: 1:20
 */
//@WebServlet("/file-tree")
public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = -6518190301785071457L;
    private String imgDir = "icon-set" + File.separator, initDir = "D:";

    public void setImgDir(String imgDir) {
        this.imgDir = imgDir;
    }

    public void setInitDir(String initDir) {
        this.initDir = initDir;
    }

    public MainServlet() {
        //srvc = (IService) FileTreeAppContext.getInstance().getBean("useThis");
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext( req.getSession().getServletContext() );
        IService srvc = (IService)context.getBean( "useThis" );

        final String headFragment = "<HTML>\n" +
                "<HEAD>\n" +
                "   <link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\" />\n" +
                "   <title>File-tree (Remote File Browser)</title>\n" +
                "</HEAD>\n" +
                "<BODY>\n" +
                "   <h1 class=\"page-title\">" + getMessage() + "</h1>\n" +
                "   <br /><br />",
                tailFragment  = "   </table>\n" +
                        "</BODY>\n" +
                        "</HTML>",
                encoding      = "UTF-8";

        resp.setContentType( "text/html" );
        resp.setCharacterEncoding( encoding );   // For proper displaying of russian characters (Cyrillic)
        PrintWriter writer = resp.getWriter();
        try {
            writer.println( headFragment );

            String dirName = req.getParameter( "path" );
            if (dirName == null || dirName.isEmpty())
                dirName = initDir;
            else
                dirName = URLDecoder.decode( dirName, encoding );

            File target = new File( dirName );
            TreeSet<ChangeDescription> cmpResult = null;
            String encodedUrl;
            if (target == null || !target.exists())
                writer.println( "<h2>Error: Directory not found.</h2>" );
            else {
                writer.println( "<h2><img src=\"" + imgDir + "Type-Folder-Open.ico\" " +
                        "alt=\"Opened directory \" " +
                        "width=\"32px\" " +
                        "height=\"32px\" />There is the content of '" + dirName + "':</h2>\n" +
                        "<table border=\"0\">" );

                    // logic
                Set<FileIndex>  state1 = null,
                                state2 = null;
                try {

                    state2 = srvc.describeIt( target );

                    if (srvc.wasSerialized( target )) {
                        state1 = srvc.loadDescription( target );
                        cmpResult = (TreeSet<ChangeDescription>)srvc.compareStates( state1, state2 );
                    }

                    //TODO: only if 'auto-save' option is checked
                    srvc.saveDescription( state2, target );
                } catch (ServiceException exc) {
                    exc.printStackTrace();
                } finally {
                    // this block will do its work even if DB-server has not started
                    if (state1 == null && state2 != null) {
                        // create new description for all files (mark them NOT CHANGED)
                        cmpResult = new TreeSet<ChangeDescription>();
                        for (FileIndex fi : state2)
                            cmpResult.add( new ChangeDescription( fi ) );
                    }
                }

                // .. (parent directory)
                FileIndex tmpFI = new FileIndex();
                tmpFI.setFullName( dirName );
                encodedUrl = URLEncoder.encode( tmpFI.getPath(), encoding );
                writer.println( "<tr>\n" +
                                "   <td />\n" +
                                "   <td><a href=\"" + req.getContextPath() + "file-tree?path=" + encodedUrl + "\">[..]</a></td>\n" +
                                "   <td />\n" +
                                "</tr>" );
            }

                // list other files (with difference report)
            if (cmpResult == null || cmpResult.isEmpty())
                writer.println( "<tr><td /><td><h3 class=\"description\">(Empty directory)</h3></td></tr>" );
            else {
                String fileName;
                for (ChangeDescription cd : cmpResult) {
                    fileName = cd.getFilename();
                    encodedUrl = URLEncoder.encode( dirName + File.separator + fileName, encoding );
                    writer.println("<tr>");
                    if (cd.isFile())
                        writer.println( "<td><img src=\"" + imgDir + "Type-Document.ico\" " +
                                "alt=\"T:Document\" " +
                                "bgcolor=\"white\" " +
                                "width=\"16px\" " +
                                "height=\"16px\" /></td>\n" +
                                "<td>" + fileName + "</td>" +
                                "<td>" + cd.getSize() + " B</td>\n" );
                    else
                        writer.println( "<td><img src=\"" + imgDir + "Type-Folder.ico\" " +
                                "alt=\"T:Directory\" " +
                                "bgcolor=\"yellow\" " +
                                "width=\"16px\" " +
                                "height=\"16px\" /></td>\n" +
                                "<td><a href=\"" + req.getContextPath() + "file-tree?path=" + encodedUrl + "\">" + fileName + "</a></td>\n" +
                                "<td />" );

                    // visual representation of a change
                    String iconName = imgDir,
                           associatedColor = null;
                    switch (cd.getChange()) {
                        case NOT_CHANGED:
                            iconName = imgDir + "File-Not-changed.ico";
                            associatedColor = "white";
                            break;
                        case MODIFIED:
                            iconName = imgDir + "File-Modified.ico";
                            associatedColor = "yellow";
                            break;
                        case CREATED:
                            iconName = imgDir + "File-Created.ico";
                            associatedColor = "green";
                            break;
                        case DELETED:
                            iconName = imgDir + "File-Deleted.ico";
                            associatedColor = "red";
                            break;
                        case ROLLBACKED:
                            iconName = imgDir + "File-Rollbacked.ico";
                            associatedColor = "blue";
                            break;
                    }
                    writer.println( "<td><img src=\"" + iconName + "\" " +
                            "alt=\"" + cd + "\" " +
                            "bgcolor=\"" + associatedColor + "\" " +
                            "width=\"16px\" " +
                            "height=\"16px\" >" +
                            "<h3 class=\"description\" " +
                            "color=\"" + associatedColor + "\">" + cd +
                            "</h3>" +
                            "</img>" +
                            "</td>\n" +
                            "</tr>" );
                }
            }

            writer.println( tailFragment );
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    /**
     *
     * @param req Request from page
     * @param resp Response from server
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //doGet( req, resp );
    }

    /**
     *
     * @return Welcome message
     */
    public static String getMessage() {
        return "Welcome, Dear Visitor!";
    }
}
