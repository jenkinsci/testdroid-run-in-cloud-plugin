# Testdroid Run in Cloud Plugin

## About plugin

Testdroid Run in Cloud (RiC) is a Jenkins plugin to launch your
Testdroid test runs directly from your Jenkins job.

## Build plugin

To build the RiC plugin simply run Maven at the root of the repo.

## Downloads

[Latest binary](bin/testdroid-run-in-cloud-1.0.9.hpi)

## Using the Run In Cloud Plugin

Once you have Jenkins properly installed, download Testdroid Run In
Cloud Plugin from under *./bin*-directory and open your Jenkins main
page. Then navigate in Jenkins 'main menu' to **Manage Jenkins -> Manage
plugins** to start configuring your plugins.

Click **Advanced -> Upload Plugin** to install recently downloaded
Testdroid Run in Cloud Plugin. After you've installed this plugin,
restart Jenkins and get back to start the configuration of this
plugin. The plugin adds a new build step that can be used in any
Jenkins job to launch builds in Testdroid Cloud and/or Testdroid
Private Cloud.

Click **Manage Jenkins -> Configure System** (on top of that list) and
you'll find a Testdroid Cloud section where you can enter your login
credentials (email and password) and then press **Authorize** button to
validate your account details and access.

### Run In Cloud - Build Step(s) for Android and iOS

To get started, you can open existing Jenkins job or create a new
one. From the **Job Configuration -> Build** select **Run tests in
Testdroid Cloud**.

Now you can select your target project (must exist in Testdroid Cloud)
and enter the name for your test run. The next ones are the
Application and Test where you specify the files that will be uploaded
in Testdroid Cloud. If you need to further customize project settings
click the "Edit project on Testdroid Cloud website" button and it will
open Testdroid Cloud webpage with access to configure your project
details.

## Notifications from Testdroid Cloud about Test Run

There's two simple ways to get notifications from Testdroid Cloud that
your test runs are finished. Whether you are running just on few
devices or hundreds different devices simultaneously, both of these
ways provide extremely easy and straightforward way to get results
back to your Jenkins environment.

### API CALL

Jenkins is polling Testdroid Cloud for the results. Test results are
fetched from Testdroid Cloud to Jenkins workspace after all your tests
are finished.

### HOOK URL

Testdroid Cloud sends post message to the specified URL when test run
is finished. Testdroid Run in Cloud Plugin listens these messages in
default location and then downloads results when post message
arrives. User can change the URL in the following section:

    POST message body: testRunId=%s&projectId=%s&status=FINISHED

**Note!**  This requires your HOOK URL is reachable from Internet
e.g. Jenkins instance can be reached from Testdroid Cloud.

In addition to hook URL, you can set up specific folder where results
and data will be stored, timeout for finalization message (timeout for
results) as well as if you want to include screenshots and other
graphical data from your test runs.
