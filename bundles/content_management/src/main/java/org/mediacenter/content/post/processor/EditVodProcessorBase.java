package org.mediacenter.content.post.processor;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.mediacenter.resource.ChannelNodeLookup;
import org.mediacenter.resource.MediaCenterResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Base class called during POST operations for mediacenter:vod resource types. */
public class EditVodProcessorBase extends AbstractPostProcessor implements SlingPostProcessor
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doProcess(SlingHttpServletRequest request, List<Modification> changes) throws Exception
    {
        logger.info("doProcess{}", request.getResource().getResourceType());

        Session session = request.getResourceResolver().adaptTo(Session.class);

        // NODE: getResource().getPath() may be the "month", not the actual content
        String videoPath = request.getResource().getPath();
        Node videoNode = session.getNode(videoPath);
        if (!request.getResource().isResourceType(MediaCenterResourceType.VOD))
        {
//            String patternString = videoPath + "(.*?)/sling:resourceType$";
            // find the path to the video form the changes list
            for (Modification m : changes)
            {
                if (session.nodeExists(m.getSource()))
                {
                    Node mNode = session.getNode(m.getSource());
                    if (mNode.hasProperty(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY) &&
                            mNode.getProperty(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY).getString()
                                    .equals(MediaCenterResourceType.VOD))
                    {
                        videoNode = mNode;
                        break;
                    }
                }
            }
        }


        NodeType[] mixins = videoNode.getMixinNodeTypes();

        videoNode.addMixin("mix:shareable");
//        videoNode.addMixin("mix:created");
//        videoNode.addMixin("mix:lastModified");

        setAccessLevelPermissions(request, session, videoNode);
    }

    protected void setAccessLevelPermissions(SlingHttpServletRequest request, Session session, Node videoNode)
            throws RepositoryException
    {
        PrincipalManager principalManager = AccessControlUtil.getPrincipalManager(session);

        String[] grantedPrivilegeNames = null;
        String[] deniedPrivilegeNames = null;


        RequestParameter privacyFlag = request.getRequestParameter("active");
        if (privacyFlag != null && "false".equals(privacyFlag.getString()))
        {
            // rep:principalName=anonymous, new String[] {"jcr:read"}

            grantedPrivilegeNames = null;
            deniedPrivilegeNames = new String[] { "jcr:read" }; //new String[] {"jcr:all"};

            AccessControlUtil.replaceAccessControlEntry(session, videoNode.getPath(),
                    principalManager.getPrincipal("anonymous"),
                    grantedPrivilegeNames, deniedPrivilegeNames, null, null);
        }

        else
        {
            AccessControlUtil.replaceAccessControlEntry(session, videoNode.getPath(),
                    principalManager.getPrincipal("anonymous"), null, null, null, null);
            videoNode.removeMixin("rep:AccessControllable");

        }
    }


}
