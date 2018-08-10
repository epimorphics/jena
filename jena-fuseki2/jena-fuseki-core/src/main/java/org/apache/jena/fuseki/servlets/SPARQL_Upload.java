/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format ;

import java.io.PrintWriter ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.commons.fileupload.servlet.ServletFileUpload ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.system.Upload;
import org.apache.jena.fuseki.system.UploadDetailsWithName;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.web.HttpSC ;

/**
 * Upload data into a graph within a dataset. This is fuseki:serviceUpload.
 * 
 * It is better to use GSP POST with the body being the content.
 * 
 * This class work with general HTML form file upload wherte  has the name somewhere in the form and that may be
 * after the data.
 * 
 * With sophisticated use of {@link ServletFileUpload}, it is possible to stream to disk
 * avoid an in-memory copy. The whole form is processed to find the fields before parsing starts
 * and potentially is is several files.
 * 
 * This all makes transactional stream-parsing quite complex and it requires an abort. 
 * 
 * Consider this service useful for small files and use GSP POST for large ones.
 */
public class SPARQL_Upload extends ActionService
{
    public SPARQL_Upload() {
        super() ;
    }

    // Methods to respond to.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response) ;
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setCommonHeadersForOptions(response) ;
        response.setHeader(HttpNames.hAllow, "OPTIONS,POST") ;
        response.setHeader(HttpNames.hContentLengh, "0") ;
    }

    @Override
    protected void validate(HttpAction action)
    {}

    @Override
    protected void perform(HttpAction action) {
        // Only allows one file in the upload.
        boolean isMultipart = ServletFileUpload.isMultipartContent(action.request);
        if ( ! isMultipart )
            ServletOps.error(HttpSC.BAD_REQUEST_400 , "Not a file upload") ;

        long count = upload(action, Fuseki.BaseUpload) ;
        ServletOps.success(action) ;
        try {
            action.response.setContentType("text/html") ;
            action.response.setStatus(HttpSC.OK_200);
            PrintWriter out = action.response.getWriter() ;
            out.println("<html>") ;
            out.println("<head>") ;
            out.println("</head>") ;
            out.println("<body>") ;
            out.println("<h1>Success</h1>");
            out.println("<p>") ;
            out.println("Triples = "+count + "\n");
            out.println("<p>") ;
            out.println("</p>") ;
            out.println("<button onclick=\"timeFunction()\">Back to Fuseki</button>");
            out.println("</p>") ;
            out.println("<script type=\"text/javascript\">");
            out.println("function timeFunction(){");
            out.println("window.location.href = \"/fuseki.html\";}");
            out.println("</script>");
            out.println("</body>") ;
            out.println("</html>") ;
            out.flush() ;
            ServletOps.success(action) ;
        }
        catch (Exception ex) { ServletOps.errorOccurred(ex) ; }
    }

    // Also used by SPARQL_REST
    static public long upload(HttpAction action, String base) {
        if ( action.isTransactional() )
            return uploadTxn(action, base) ;
        else
            return uploadNonTxn(action, base) ;
    }

    /** Non-transaction - buffer to a temporary graph so that parse errors
     * are caught before inserting any data.
     */
    private static long uploadNonTxn(HttpAction action, String base) {
        UploadDetailsWithName upload = Upload.multipartUploadWorker(action, base) ;
        String graphName = upload.graphName ;
        DatasetGraph dataTmp = upload.data ;
        long count = upload.count ;

        if ( graphName == null )
            action.log.info(format("[%d] Upload: %d Quads(s)",action.id, count)) ;
        else
            action.log.info(format("[%d] Upload: Graph: %s, %d triple(s)", action.id, graphName,  count)) ;

        Node gn = null ;
        if ( graphName != null ) {
            gn = graphName.equals(HttpNames.valueDefault)
                ? Quad.defaultGraphNodeGenerated
                : NodeFactory.createURI(graphName) ;
        }

        action.beginWrite() ;
        try {
            if ( gn != null )
                FusekiLib.addDataInto(dataTmp.getDefaultGraph(), action.getActiveDSG(), gn) ;
            else
                FusekiLib.addDataInto(dataTmp, action.getActiveDSG()) ;

            action.commit() ;
            return count ;
        } catch (RuntimeException ex)
        {
            // If anything went wrong, try to backout.
            try { action.abort() ; } catch (Exception ex2) {}
            ServletOps.errorOccurred(ex.getMessage()) ;
            return -1 ;
        }
        finally { action.end() ; }
    }

    // XXX Improve.  Needs Upload code rework.
    /** 
     * Transactional - we'd like better handle the data and go straight to the destination, with an abort on parse error.
     * Use Graph Store protocol for bulk uploads.
     */
    private static long uploadTxn(HttpAction action, String base) {
        return uploadNonTxn(action, base) ;
    }

}
