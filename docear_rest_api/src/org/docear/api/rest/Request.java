package org.docear.api.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public class Request {
	private final RequestMethod method;
	
	public Request(RequestMethod method) {
		this.method = method;
	}

	public RequestMethod getRequestMethod() {
		return method;
	}
	
	public Response query(String query) throws Exception {
		//TODO build HTTP Header with or w/o security token
		//TODO parse HTTP response and status code
				
//		Socket sock = new Socket("intranet.eikel.gymszbad.de", 443);
//		try {
//			
//			
//			//Scanner reader = new Scanner(sock.getInputStream());
//			InputStream is = sock.getInputStream();
//			int chr = -1;
////			while((chr = reader.read()) > -1) {
////				System.out.print((char)chr);
////			}
//			
//			byte[] randomCookie = new byte[]{ 123, 6, (byte)165, 35, 40, 20, (byte) 158, (byte) 178, 47, 105, 105, 19, 21, (byte) 171, (byte) 249, (byte) 238, (byte) 218, 122, (byte) 153, 57, (byte) 160, 106, (byte) 130, 41, 53, (byte) 242, (byte) 249, 112 };
//			
//			DataOutputStream os = new DataOutputStream(sock.getOutputStream());
//			//Content type
//			os.write(22);
//			//TLS version major
//			os.write(3);
//			//TLS version minor
//			os.write(1);
//			
//			//length hi byte			
//			os.write(0);
//			//length lo byte
//			os.write(47);
//			
//			//Handshake message
//			//message type
//			os.write(1);
//			
//			//message length hi hi byte
//			os.write(0);
//			//message length hi byte
//			os.write(0);
//			//message length lo byte
//			os.write(43);
//			
//			//handshake version major
//			os.write(3);
//			//handshake version minor
//			os.write(1);
//			os.writeInt(1330598190);
//			os.write(randomCookie);
//			//SessionID length
//			os.write(0);
//			//cipher length 2 bytes
//			os.write(0);
//			os.write(2);
//			//cipher codes (2 bytes each)
//			os.write(0);
//			os.write(4);
//			//compression methods length
//			os.write(1);
//			//compression methods
//			os.write(0); //NULL
//			//extension length
//			os.write(0);
//			os.write(0);
//		
//			os.flush();
//			
//			while((chr = is.read()) > -1) {
//				//System.out.print(">"+(char)chr);
//				System.out.println(Integer.toHexString(chr));
//			}
//			os.close();
//			is.close();
//		} 
//		finally {
//			sock.close();
//			System.out.println("The End ...");
//		}
		
//		InputStream inStream = this.getClass().getResourceAsStream("/intranet.eikel.gymszbad.de.crt");
//		CertificateFactory cf = CertificateFactory.getInstance("X.509");
//		X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
//		inStream.close();
//		
//		//SSLContext.getDefault().getSocketFactory().createSocket("intranet.eikel.gymszbad.de", 443);
//		
//		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//		ks.load(null, "docear".toCharArray());
//		ks.setCertificateEntry("intranet.eikel.gymszbad.de", cert);
//		
//		SSLContext.getDefault().getSocketFactory().createSocket("intranet.eikel.gymszbad.de", 443);
		
//		String cacert = "mytest.cer";
//	    String lfcert = "lf_signed.cer";
//	    String lfstore = "lfkeystore";
//	    char[] lfstorepass = "wshr.ut".toCharArray();
//	    char[] lfkeypass = "wshr.ut".toCharArray();
//	    
//	    CertificateFactory cf = CertificateFactory.getInstance("X.509");
//	    
//	    FileInputStream in1 = new FileInputStream(cacert);
//	    java.security.cert.Certificate cac = cf.generateCertificate(in1);
//	    in1.close();
//	    
//	    FileInputStream in2 = new FileInputStream(lfcert);
//	    java.security.cert.Certificate lfc = cf.generateCertificate(in2);
//	    in2.close();
//	    
//	    java.security.cert.Certificate[] cchain = { lfc, cac };
//	    
//	    FileInputStream in3 = new FileInputStream(lfstore);
//	    KeyStore ks = KeyStore.getInstance("JKS");
//	    ks.load(in3, lfstorepass);
//	    PrivateKey prk = (PrivateKey) ks.getKey("lf", lfkeypass);
//	    ks.setKeyEntry("lf_signed", prk, lfstorepass, cchain);
//	    FileOutputStream out4 = new FileOutputStream("lfnewstore");
//	    ks.store(out4, "newpass".toCharArray());
//	    out4.close();
	    
		
//		CertificateFactory cf = CertificateFactory.getInstance("X.509");
//	    List mylist = new ArrayList();
//	    InputStream in = this.getClass().getResourceAsStream("/intranet.eikel.gymszbad.de.crt");
//	    Certificate c = cf.generateCertificate(in);
//	    mylist.add(c);
//
	    //CertPath cp = cf.generateCertPath(mylist);
	    
	    String filename = System.getProperty("java.home")
	            + "/lib/security/cacerts".replace('/', File.separatorChar);
//	        FileInputStream is = new FileInputStream(filename);
//	        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	        String password = "changeit";
//	        keystore.load(null, password.toCharArray());
//
//	        keystore.setCertificateEntry("intranet.eikel.gymszbad.de",c);
//	        
//	        
//	        PKIXParameters params = new PKIXParameters(keystore);
//
//	        params.setRevocationEnabled(false);

//	        CertPathValidator certPathValidator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
//	        CertPath certPath = cf.generateCertPath(mylist);
	        
	        //keystore.store(new FileOutputStream(filename), password.toCharArray());
	       
//	        CertPathValidatorResult result = certPathValidator.validate(certPath, params);
//
//	        PKIXCertPathValidatorResult pkixResult = (PKIXCertPathValidatorResult) result;
//	        TrustAnchor ta = pkixResult.getTrustAnchor();
//	        X509Certificate cert = ta.getTrustedCert();
//	        System.out.println(cert.getType());
	     SSLContext context = SSLContext.getInstance("SSL");
	     context.init(new X509KeyManager[] { new MyKeyManager(filename, password.toCharArray(), "") }, new X509TrustManager[]{ new MyTrustManager() }, null);
	       
	     
	     Socket sock = context.getSocketFactory().createSocket("api.docear.org",443);
//
	       OutputStreamWriter writer = new OutputStreamWriter(sock.getOutputStream());
	       writer.write("GET /infotext.html HTTP/1.1\r\n");
	       writer.flush();
//
//	       BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	       
		URL url = new URL("https://api.docear.org/");
		URLConnection conn = url.openConnection();
//	    HttpsURLConnection uc = (HttpsURLConnection)url.openConnection();
//	    uc.getdefa
//	    uc.connect();
//	    try {
//	        Certificate cert[] = uc.getServerCertificates();
//	        for (int i = 0; i < cert.length; i++) {
//	          System.out.println
//	            (" " + (i + 1) + ":  " + cert[i].toString());
//	          System.out.println();
//	        }            
//	      } catch (Exception e) {
//	        System.out.println("Problem in verifying the servers certificate");
//	        System.out.println(e.toString());
//	        e.printStackTrace(System.out);
//	      }
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        
        while ((inputLine = reader.readLine()) != null) {
            System.out.println(inputLine);
        }
//        writer.close();
//        in.close();
//        sock.close();
		return null;
	}

}
