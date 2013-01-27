package org.mediacenter.content.post.processor;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;

import junit.framework.TestCase;

public class AbstractPostProcessorTest extends TestCase
{
    private AbstractPostProcessor processor = new AbstractPostProcessor();
    private Method canProcessResourceType;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        canProcessResourceType = getMethod("canProcessResourceType", String.class);
    }

    public void testResourceTypesFilter_WithNullResources() throws Exception {
        assertEquals(true, canProcessResourceType.invoke(processor, "myResourceType"));
    }

    public void testResourceTypeFilter_WithOneResource() throws Exception {
        MockComponentContext ctx = new MockComponentContext();
        Dictionary d = new Hashtable<String, Object>();
        d.put( "sling.servlet.resourceTypes", "my:resourceType");
        ctx.setProperties( d );

        Method activateOP = getMethod("activate", ComponentContext.class);
        activateOP.invoke( processor, ctx );

        assertEquals(false, canProcessResourceType.invoke(processor, "AAA"));
        assertEquals(true, canProcessResourceType.invoke(processor, "my:resourceType"));
    }

    public void testResourceTypeFilter_WithMoreResources() throws Exception {
        MockComponentContext ctx = new MockComponentContext();
        Dictionary d = new Hashtable<String, Object>();
        String types[] = {"my:resourceType1", "my:resourceType2"};
        d.put( "sling.servlet.resourceTypes", types );
        ctx.setProperties( d );

        Method activateOP = getMethod("activate", ComponentContext.class);
        activateOP.invoke( processor, ctx );

        assertEquals(false, canProcessResourceType.invoke(processor, "AAA"));
        assertEquals(true, canProcessResourceType.invoke(processor, "my:resourceType1"));
        assertEquals(true, canProcessResourceType.invoke(processor, "my:resourceType2"));
        assertEquals(false, canProcessResourceType.invoke(processor, "my:resourceType_333"));
    }

    public void testResourceTypeWithNullValue() throws Exception {
        MockComponentContext ctx = new MockComponentContext();
        Dictionary d = new Hashtable<String, Object>();
        String types[] = {"my:resourceType1", "my:resourceType2"};
        d.put( "sling.servlet.resourceTypes", types );
        ctx.setProperties( d );

        Method activateOP = getMethod("activate", ComponentContext.class);
        activateOP.invoke( processor, ctx );

        Object nullObj = null;
        assertEquals(false, canProcessResourceType.invoke(processor, nullObj));
        assertEquals(false, canProcessResourceType.invoke(processor, "AAA"));
        assertEquals(true, canProcessResourceType.invoke(processor, "my:resourceType1"));
        assertEquals(true, canProcessResourceType.invoke(processor, "my:resourceType2"));
        assertEquals(false, canProcessResourceType.invoke(processor, "my:resourceType_333"));
    }


    private Method getMethod(String name, Class... parameterTypes) {
        try {
            Method m = AbstractPostProcessor.class.getDeclaredMethod(name,
                parameterTypes);
            m.setAccessible(true);
            return m;
        } catch (Throwable t) {
            fail(t.toString());
            return null; // compiler wants this
        }
    }

    private static class MockComponentContext implements ComponentContext {
        private Dictionary properties;

        public void setProperties( Dictionary props ) {
            properties = props;
        }

        public Dictionary getProperties()
        {
            return properties;
        }

        public Object locateService(String s)
        {
            return null;
        }

        public Object locateService(String s, ServiceReference serviceReference)
        {
            return null;
        }

        public Object[] locateServices(String s)
        {
            return new Object[ 0 ];
        }

        public BundleContext getBundleContext()
        {
            return null;
        }

        public Bundle getUsingBundle()
        {
            return null;
        }

        public ComponentInstance getComponentInstance()
        {
            return null;
        }

        public void enableComponent(String s)
        {

        }

        public void disableComponent(String s)
        {

        }

        public ServiceReference getServiceReference()
        {
            return null;
        }
    }
}
