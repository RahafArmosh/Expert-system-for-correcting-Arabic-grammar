
package com.alp.webservice;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import alp.Alp;


@Path("/api")
public class UYResource {

	/**
	 * @param args
	 *            the command line arguments
	 */
	@Path("/get")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getUserClaims(@Context ServletContext servletContext,@QueryParam("c") String c) throws InterruptedException {
		Alp alp = new Alp();

		boolean c1 = false;
		String x="";
		
		// TODO code application logic here
		//c = alp.posTag(c).split(" ")[1].split("_")[1];
		Main2 n=new Main2();
		
	
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			
			 x=n.test(c);
			System.out.println(x);
			return Response.ok(mapper.writeValueAsString(x)).build();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

///////////////////////////////////////////////////////////////////////////////
	@Path("/gett2")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getUser(@Context ServletContext servletContext,@QueryParam("c1") String c1) throws InterruptedException {
		Alp alp = new Alp();

		boolean c11 = false;
		String x="";
		
		// TODO code application logic here
		//c = alp.posTag(c).split(" ")[1].split("_")[1];
		Main n=new Main();
		
	
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			
			 x=n.testing(c1);
			System.out.println(x);
			return Response.ok(mapper.writeValueAsString(x)).build();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	////////////////////////////////////////////////////////////////////////////////
	@Path("/gett3")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getUser1(@Context ServletContext servletContext,@QueryParam("c2") String c2) throws InterruptedException {

		boolean c11 = false;
		String x="";
		
		// TODO code application logic here
		Main3 n=new Main3();
		
	
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			
			 x=n.testing(c2);
			System.out.println(x);
			return Response.ok(mapper.writeValueAsString(x)).build();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	//////////////////////////////////////////////////////////////////////////////
	@Path("/gett4")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getUser2(@Context ServletContext servletContext,@QueryParam("c3") String c3) throws InterruptedException {

		boolean c11 = false;
		String x="";
		
		// TODO code application logic here
		Main4 n=new Main4();
		
	
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			
			 x=n.testing(c3);
			System.out.println(x);
			return Response.ok(mapper.writeValueAsString(x)).build();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}



}
