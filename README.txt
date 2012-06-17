This bundle is an OSGI Fragment Bundle that attaches to org.apache.sling.auth.selector bundle,
and becomes a resource for that bundle, in order to show a custom login form.

To build and test:
A: FIRST TIME YOU INSTALL THIS BUNDLE
1. mvn clean install -PautoInstallBundle
   1.1 Check that the status of this bundle is "Installed" in Felix Web Console
2. In Felix Web Console locate org.apache.sling.auth.selector bundle:
   2.1 Stop the bundle
   2.2 Refresh package imports
   2.3 Start bundle
3. Check that the status of this bundle has changed from "Installed" into fragment.

B: CONSECUTIVE INSTALLS
NOTE: once this bundle is recognized as a fragment, you should be fine executing just step 1:
1. mvn clean install -PautoInstallBundle