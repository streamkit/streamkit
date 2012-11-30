package org.mediacenter.content.post.processor;

import java.util.Calendar;
import java.util.List;

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

        Node n = session.getNode(request.getResource().getPath());
        NodeType[] mixins = n.getMixinNodeTypes();

        n.addMixin("mix:shareable");
//        n.addMixin("mix:created");
//        n.addMixin("mix:lastModified");

        setAccessLevelPermissions(request, session, n);
    }

    protected void setAccessLevelPermissions(SlingHttpServletRequest request, Session session, Node currentNode)
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

            AccessControlUtil.replaceAccessControlEntry(session, request.getResource().getPath(),
                    principalManager.getPrincipal("anonymous"),
                    grantedPrivilegeNames, deniedPrivilegeNames, null, null);
        }

        else
        {

            AccessControlUtil.replaceAccessControlEntry(session, request.getResource().getPath(),
                    principalManager.getPrincipal("anonymous"), null, null, null, null);
            currentNode.removeMixin("rep:AccessControllable");

        }
    }


}
