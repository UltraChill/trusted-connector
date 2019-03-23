/*-
 * ========================LICENSE_START=================================
 * ids-webconsole
 * %%
 * Copyright (C) 2018 Fraunhofer AISEC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package de.fhg.aisec.ids.webconsole.api;

import de.fhg.aisec.ids.api.acme.AcmeClient;
import de.fhg.aisec.ids.api.acme.AcmeTermsOfService;
import de.fhg.aisec.ids.api.settings.ConnectorConfig;
import de.fhg.aisec.ids.api.settings.Settings;
import de.fhg.aisec.ids.webconsole.WebConsoleComponent;
import de.fhg.aisec.ids.webconsole.api.data.Cert;
import de.fhg.aisec.ids.webconsole.api.data.Identity;
import de.fhg.aisec.ids.webconsole.api.helper.ProcessExecutor;
import io.swagger.annotations.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;  
import javax.ws.rs.GET;  
import javax.ws.rs.Path;  
import javax.ws.rs.Produces;  
import javax.ws.rs.core.Response;  
import javax.ws.rs.core.Response.ResponseBuilder;  



/**
 * REST API interface for managing upload and download of license PDF files in the connector.
 *
 * <p>The API will be available at http://localhost:8181/cxf/api/v1/licenses/<method>.
 *
 * @author Hendrik Meyer zum Felde (hendrik.meyerzumfelde@aisec.fraunhofer.de)
 */
@Path("/licenses")
@Api(value = "Licenses")
public class LicenseApi {
  private static final Logger LOG = LoggerFactory.getLogger(LicenseApi.class);
  private static final String LICENSE_STORAGE_PATH = System.getProperty("karaf.etc");
   
   
  @POST
  @Path("/upload_license")
  @ApiOperation(value = "Installs a new trusted public key certificate.")
  @ApiImplicitParams({
    @ApiImplicitParam(dataType = "java.io.File", name = "attachment", paramType = "formData")
  })
  @Produces(MediaType.TEXT_HTML)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public String uploadLicense(
      @ApiParam(hidden = true, name = "attachment") @Multipart("file") Attachment attachment)
      throws IOException {
//      if (etcDir != null) {
//      File f = new File(etcDir + File.separator + fileName);}      
 
    File etcDirFile = new File(LICENSE_STORAGE_PATH); 
    File licenseDir = new File(etcDirFile, "licenses");     
    if (! licenseDir.exists()){
        licenseDir.mkdir();
    }
//    String filename = attachment.getContentDisposition().getParameter("filename");
    File tempFile = File.createTempFile("license", ".pdf", licenseDir);
    String filenameInStore = tempFile.getName();
    try (OutputStream out = new FileOutputStream(tempFile);
        InputStream in = attachment.getObject(InputStream.class)) {
      int read;
      byte[] bytes = new byte[1024];
      while ((read = in.read(bytes)) != -1) {
        out.write(bytes, 0, read);
      }
    } 
      return "License stored successfully at: " + "<a href=\"/cxf/api/v1/licenses/get_license/" + filenameInStore + "\">Download</a>";
  }
  
  @GET
  @Path("/get_license/{id}")
  @ApiOperation(value = "Download stored license.")
//  @Produces("application/pdf")    
  public Response getLicense(@PathParam("id") String id)
      throws IOException {
    File etcDirFile = new File(LICENSE_STORAGE_PATH);  
    File licenseDir = new File(etcDirFile, "licenses");  
    File file = new File(licenseDir, id);
    if (file.exists()){    
        ResponseBuilder response = Response.ok((Object) file);  
        response.header("Content-Disposition","attachment; filename=\"" + id + "\"");        
        response.header("Content-Type","application/pdf"); 
        return response.build();  
    }
    ResponseBuilder response = Response.status(Response.Status.NOT_FOUND).entity("License not found for ID: " + id);
    return response.build();
  }

}
