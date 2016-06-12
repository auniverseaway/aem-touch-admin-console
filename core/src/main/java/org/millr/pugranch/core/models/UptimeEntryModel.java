package org.millr.pugranch.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = SlingHttpServletRequest.class)
public class UptimeEntryModel {
	
	private ResourceResolver resourceResolver;
    
    private String path;
    
    private Resource entryResource;
    
    private ValueMap valueMap;
    
    public UptimeEntryModel(SlingHttpServletRequest request) {
    	this.resourceResolver = request.getResourceResolver();
    	this.path = request.getParameter("path");
    	
    	if (this.path != null) {
    		this.entryResource = resourceResolver.getResource(this.path);
    		this.valueMap = this.entryResource.getValueMap();
    	}
    }
    
    public Resource getEntryResource() {
    	return entryResource;
    }
    
    public Long getYear() {
    	return valueMap.get("year", Long.class);
    }
    
    public String getMonth() {
    	return valueMap.get("month", String.class);
    }
    
    public Double getPercent() {
    	return valueMap.get("percent", Double.class);
    }
}
