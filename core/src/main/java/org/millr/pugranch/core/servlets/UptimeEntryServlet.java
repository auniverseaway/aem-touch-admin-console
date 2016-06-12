package org.millr.pugranch.core.servlets;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.CharEncoding;

import com.day.cq.commons.jcr.JcrConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class UptimeEntryServlet.
 */
@SlingServlet(
    paths = {"/bin/pugranch/uptimeEntry"},
    extensions = {"html"},
    methods = {"POST"}
)
public class UptimeEntryServlet extends SlingAllMethodsServlet {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(UptimeEntryServlet.class);
	
	/** The Serial Version UID. */
	private static final long serialVersionUID = -5957218834920785823L;
	
	/** The Constant UPTIME_CALENDAR_PATH. */
	private static final String UPTIME_CALENDAR_PATH = "/etc/pugranch/public/uptimeCalendar";
	
	/** The resource resolver. */
	private ResourceResolver resourceResolver;
	
	/* (non-Javadoc)
	 * @see org.apache.sling.api.servlets.SlingAllMethodsServlet#doPost(org.apache.sling.api.SlingHttpServletRequest, org.apache.sling.api.SlingHttpServletResponse)
	 */
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		
		resourceResolver = request.getResourceResolver();
		
		// Grab our params from the request.
		final String redirectPath = request.getParameter(":redirect");
		final String month = request.getParameter("month");
		final String yearString = request.getParameter("year");
		final Long year = Long.parseLong(yearString);
		
		// Get and fix the double value coming from Coral's Step Counter
		Double percent = Double.parseDouble(request.getParameter("percent"));
		DecimalFormat df = new DecimalFormat("###.##");
		df.setRoundingMode(RoundingMode.HALF_EVEN);
		percent = Double.parseDouble(df.format(percent));
		
		// Concat to create a semi-unique name
		final String nodeName = yearString + "-" + month;
		
		// Set properties for the entry.
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
		properties.put("month", month);
		properties.put("year", year);
		properties.put("percent", percent);
		
		// Get our parent resource
		Resource parentResource = getParentResouce();
		
		// Try to get the existing node
		String existingPath = UPTIME_CALENDAR_PATH + "/" + nodeName;
		Resource uptimeEntry = resourceResolver.getResource(existingPath);
		
		if (uptimeEntry != null) {
			LOGGER.info("Saving existing uptime entry.");
			ModifiableValueMap existingProperties = uptimeEntry.adaptTo(ModifiableValueMap.class);
			existingProperties.putAll(properties);
			setMixin(uptimeEntry, NodeType.MIX_LAST_MODIFIED);
		} else {
			LOGGER.info("Creating New Uptime Entry.");
			uptimeEntry = resourceResolver.create(parentResource, nodeName, properties);
			setMixin(uptimeEntry, NodeType.MIX_CREATED);
		}
		
		resourceResolver.commit();
		resourceResolver.close();
		
		response.sendRedirect(redirectPath);
		
		// If you prefer a JSON response, uncomment this.
		//sendResponse(response, 200, "success", "Item has been updated");
		
	}
	
	
	/**
	 * Gets the parent resouce.
	 * If the parent resource (to hold our entries) does not exist, create it.
	 *
	 * @return the parent resouce
	 */
	private Resource getParentResouce() {
		Resource parentResource = resourceResolver.getResource(UPTIME_CALENDAR_PATH);
		if(parentResource == null) {
			Resource publicResource = resourceResolver.getResource("/etc/pugranch/public");
			Map<String,Object> properties = new HashMap<String,Object>();
			properties.put(JcrConstants.JCR_PRIMARYTYPE, "sling:OrderedFolder");
			try {
				parentResource = resourceResolver.create(publicResource, "uptimeCalendar", properties);
				resourceResolver.commit();
			} catch (PersistenceException e) {
				e.printStackTrace();
			}
		}
		return parentResource;
	}

	/**
	 * Sets the mixin.
	 *
	 * @param post the post
	 * @param mixinName the mixin name
	 */
	private void setMixin(Resource post, String mixinName) {
		try {
        	Node postNode = post.adaptTo(Node.class);
        	postNode.addMixin(mixinName);
        } catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Send response.
	 *
	 * @param response the response
	 * @param responseCode the response code
	 * @param responseType the response type
	 * @param responseMessage the response message
	 */
	protected void sendResponse(final SlingHttpServletResponse response, int responseCode, final String responseType, final String responseMessage) {
        
        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("responseCode", responseCode);
            responseJSON.put("responseType", responseType);
            responseJSON.put("responseMessage", responseMessage);
        } catch(Exception e) {
            e.printStackTrace();
        }        
        response.setCharacterEncoding(CharEncoding.UTF_8);
        response.setContentType("application/json");
        response.setStatus(responseCode);
        
        try {
            response.getWriter().write(responseJSON.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
}