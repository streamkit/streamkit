package org.mediacenter.content.post.processor;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.osgi.service.component.ComponentContext;

/**
 * Abstract Post Processor class that reads from Service properties
 * and use them to filter the processors that need to be executed.
 */
public class AbstractPostProcessor implements SlingPostProcessor {
    private String[] resourceTypes;
    private String[] processors;

    protected void activate(ComponentContext context) {
        // context.getProperties dictionary is holding the annotations
        resourceTypes = OsgiUtil.toStringArray( context.getProperties().get("sling.servlet.resourceTypes") );
        processors = OsgiUtil.toStringArray( context.getProperties().get("sling.post.processor") );
        doActivate(context);
    }

    protected void deactivate(ComponentContext componentContext) {}

    // Abstract
    protected void doActivate(ComponentContext context) {}

    // Abstract
    protected void doDeactivate(ComponentContext context) {}

    public void process(SlingHttpServletRequest request, List<Modification> changes) throws Exception {
        // run only if resourceType and operation match

        String resourceType = "";
        if ( request.getResource().getResourceType() != Resource.RESOURCE_TYPE_NON_EXISTING ) {
            resourceType = request.getResource().getResourceType();
        } else {
            resourceType = request.getParameter("sling:resourceType");
        }


        if ( canProcessResourceType( resourceType ) &&
                canProcessOperation(changes.get(0).getType().name())) {
            doProcess(request, changes);
        }
    }

    //abstract
    protected void doProcess(SlingHttpServletRequest request, List<Modification> changes) throws Exception{}

    private Boolean canProcessResourceType( String resourceType ) {
        if ( resourceTypes == null ) {
            return true;
        }
        if ( resourceTypes.length == 0 ) {
            return true;
        }
        for ( String type : resourceTypes ) {
            if ( resourceType.equals(type) ) {
                return true;
            }
        }
        return false;
    }
    
    private Boolean canProcessOperation(String operation) {
        if ( processors == null ) {
            return true;
        }
        if ( processors.length == 0 ) {
            return true;
        }
        for ( String processor : processors ) {
            if ( operation.equals(processor) ) {
                return true;
            }
        }
        return false;
    }
}
