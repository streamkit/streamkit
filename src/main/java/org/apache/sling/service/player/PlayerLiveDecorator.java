package org.apache.sling.service.player;

/**
 * @author Cosmin Stanciu
 */
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceDecorator;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.tika.metadata.Metadata;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

public class PlayerLiveDecorator implements ResourceDecorator {

    public Resource decorate(Resource resource, HttpServletRequest httpServletRequest) {
        return this.decorate(resource);
    }

    public Resource decorate(Resource resource) {

        ResourceMetadata resourceMetadata = resource.getResourceMetadata();
        resourceMetadata.put("test", "test");
        return new PlayerLiveResource(resource, resourceMetadata);

    }

    public static final class PlayerLiveResource extends ResourceWrapper {

        private final ResourceMetadata resourceMetadata;

        public PlayerLiveResource(Resource resource, ResourceMetadata resourceMetadata) {
            super(resource);
            this.resourceMetadata = resourceMetadata;
        }

        @Override
        public ResourceMetadata getResourceMetadata() {
            return resourceMetadata;
        }
    }


}
