<p align="center">
  <a href="https://www.ej-technologies.com/products/jprofiler/overview.html"><img src="https://img.shields.io/badge/Java%20profiler-jprofiler-blue.svg" alt="Java profiler"></a>
  <a href="https://raw.githubusercontent.com/jenkinsci/testdroid-run-in-cloud-plugin/master/LICENSE"><img src="https://img.shields.io/crates/l/rustc-serialize.svg" alt="License"></a>
</p>

# Bitbar Run-in-Cloud Plugin

## About plugin

Bitbar Run-in-Cloud (RiC) is a Jenkins plugin to launch your Bitbar test runs directly from a Jenkins job.

## Build plugin

To build the RiC plugin simply run Maven at the root of the repo.

## Install

Get the latest version of the Bitbar Run-in-Cloud plugin from Jenkins' Plugin Manager. Search for 'Bitbar' and select the plugin for install. All updates are visible in this same place.

## Using the Run-in-Cloud Plugin

Once you have Jenkins properly installed, download the Bitbar Run-in-Cloud Plugin from under *./bin*-directory and open your Jenkins main page. Then navigate from Jenkins main menu to **Manage Jenkins -> Manage plugins** to start configuring your plugins.

Click **Advanced -> Upload Plugin** to install recently downloaded Bitbar Run-in-Cloud Plugin. After you've installed this plugin, restart Jenkins and get back to start the configuration of this plugin. The plugin adds a new build step that can be used in any Jenkins job to launch builds in Bitbar Cloud and/or Bitbar Private Cloud.

Click **Manage Jenkins -> Configure System** (on top of that list) and you'll find a Bitbar Cloud section where you can enter your login credentials (email and password) and then press **Authorize** button to validate your account details and access.

### Run-in-Cloud - Build Step(s) for Android and iOS

To get started, you can open existing Jenkins job or create a new one. From the **Job Configuration -> Build** select **Run tests in Bitbar Cloud**.

Now you can select your target project (must exist in Bitbar Cloud) and enter the name for your test run. The next ones are the Application and Test where you specify the files that will be uploaded in Bitbar Cloud. If you need to further customize project settings click the "Edit project on Bitbar Cloud website" button and it will open Bitbar Cloud webpage with access to configure your project details.

## Notifications from Bitbar Cloud about Test Run

There's two simple ways to get notifications from Bitbar Cloud that your test runs are finished. Whether you are running just on few devices or hundreds different devices simultaneously, both of these ways provide extremely easy and straightforward way to get results back to your Jenkins environment.

### API CALL

Jenkins is polling Bitbar Cloud for the results. Test results are fetched from Bitbar Cloud to Jenkins workspace after all your tests are finished.

### HOOK URL

Bitbar Cloud sends post message to the specified URL when test run is finished. Bitbar Run-in-Cloud Plugin listens these messages in default location and then downloads results when post message arrives. User can change the URL in the following section:

    POST message body: testRunId=%s&projectId=%s&status=FINISHED

**Note!**  This requires your HOOK URL is reachable from Internet e.g. Jenkins instance can be reached from Bitbar Cloud.

In addition to hook URL, you can set up specific folder where results and data will be stored, timeout for finalization message (timeout for results) as well as if you want to include screenshots and other graphical data from your test runs.
