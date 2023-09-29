package com.testdroid.jenkins;

import com.testdroid.api.APIException;
import com.testdroid.api.APIListResource;
import com.testdroid.api.dto.Context;
import com.testdroid.api.filter.FilterEntry;
import com.testdroid.api.model.*;
import com.testdroid.jenkins.auth.TestdroidApiUtil;
import com.testdroid.jenkins.model.TestRunStateCheckMethod;
import com.testdroid.jenkins.utils.AndroidLocale;
import com.testdroid.jenkins.utils.LocaleUtil;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.testdroid.api.dto.MappingKey.*;
import static com.testdroid.api.dto.Operand.EQ;
import static com.testdroid.api.filter.FilterEntry.falseFilterEntry;
import static com.testdroid.api.filter.FilterEntry.trueFilterEntry;
import static com.testdroid.api.model.APIDevice.OsType.UNDEFINED;
import static com.testdroid.jenkins.Messages.DEFINE_FRAMEWORK;
import static com.testdroid.jenkins.Messages.DEFINE_OS_TYPE;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Locale.US;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public interface RunInCloudDescriptorHelper {

    Logger LOGGER = Logger.getLogger(RunInCloudDescriptorHelper.class.getSimpleName());

    String DEFAULT_SCHEDULER = APITestRunConfig.Scheduler.PARALLEL.name();

    String DEFAULT_TEST_CASES_SELECT = APITestRunConfig.LimitationType.PACKAGE.name();

    String DEFAULT_LANGUAGE = LocaleUtil.formatLangCode(US);

    ListBoxModel.Option EMPTY_OPTION = new ListBoxModel.Option(EMPTY, EMPTY);

    default boolean isAuthenticated() {
        return TestdroidApiUtil.getGlobalApiClient().isAuthenticated();
    }

    default ListBoxModel doFillProjectIdItems() {
        ListBoxModel projects = new ListBoxModel();
        projects.add(EMPTY_OPTION);
        try {
            APIUser user = TestdroidApiUtil.getGlobalApiClient().getUser();
            final Context<APIProject> context = new Context<>(APIProject.class, 0, MAX_VALUE, EMPTY, EMPTY);
            context.addFilter(falseFilterEntry(READ_ONLY));
            final APIListResource<APIProject> projectResource = user.getProjectsResource(context);
            for (APIProject project : projectResource.getEntity().getData()) {
                projects.add(project.getName(), project.getId().toString());
            }
        } catch (APIException e) {
            LOGGER.log(Level.WARNING, Messages.ERROR_API());
        }
        return projects;
    }

    default ListBoxModel doFillOsTypeItems() {
        ListBoxModel osTypes = new ListBoxModel();
        osTypes.addAll(Arrays.stream(APIDevice.OsType.values())
                .map(t -> new ListBoxModel.Option(t.getDisplayName(), t.name()))
                .collect(toList()));
        return osTypes;
    }

    @POST
    default FormValidation doCheckOsType(@QueryParameter APIDevice.OsType value) {
        return value == UNDEFINED ? FormValidation.error(DEFINE_OS_TYPE()) : FormValidation.ok();
    }

    default ListBoxModel doFillSchedulerItems() {
        ListBoxModel schedulers = new ListBoxModel();
        schedulers.add(Messages.SCHEDULER_PARALLEL(), APITestRunConfig.Scheduler.PARALLEL.name());
        schedulers.add(Messages.SCHEDULER_SERIAL(), APITestRunConfig.Scheduler.SERIAL.name());
        schedulers.add(Messages.SCHEDULER_SINGLE(), APITestRunConfig.Scheduler.SINGLE.name());
        return schedulers;
    }

    default ListBoxModel doFillDeviceGroupIdItems() {
        ListBoxModel deviceGroups = new ListBoxModel();
        try {
            APIUser user = TestdroidApiUtil.getGlobalApiClient().getUser();
            final Context<APIDeviceGroup> context = new Context<>(APIDeviceGroup.class, 0, MAX_VALUE, EMPTY, EMPTY);
            HashSetValuedHashMap<String, Object> extraParams = new HashSetValuedHashMap<>();
            extraParams.put(WITH_PUBLIC, TRUE);
            context.setExtraParams(extraParams);
            final APIListResource<APIDeviceGroup> deviceGroupResource = user.getDeviceGroupsResource(context);
            for (APIDeviceGroup deviceGroup : deviceGroupResource.getEntity().getData()) {
                deviceGroups.add(String.format("%s (%d device(s))", deviceGroup.getDisplayName(),
                        deviceGroup.getDeviceCount()), deviceGroup.getId().toString());
            }
        } catch (APIException e) {
            LOGGER.log(Level.WARNING, Messages.ERROR_API());
        }
        return deviceGroups;
    }

    default ListBoxModel doFillLanguageItems() {
        ListBoxModel language = new ListBoxModel();
        for (Locale locale : AndroidLocale.LOCALES) {
            String langDisplay = String.format("%s (%s)", locale.getDisplayLanguage(),
                    locale.getDisplayCountry());
            String langCode = LocaleUtil.formatLangCode(locale);
            language.add(langDisplay, langCode);
        }
        return language;
    }

    default ListBoxModel doFillTestCasesSelectItems() {
        ListBoxModel testCases = new ListBoxModel();
        String value;
        for (APITestRunConfig.LimitationType limitationType : APITestRunConfig.LimitationType.values()) {
            value = limitationType.name();
            testCases.add(value.toLowerCase(), value);
        }
        return testCases;
    }

    default ListBoxModel doFillTestRunStateCheckMethodItems() {
        ListBoxModel items = new ListBoxModel();
        for (TestRunStateCheckMethod method : TestRunStateCheckMethod.values()) {
            items.add(method.name(), method.name());
        }
        return items;
    }

    default ListBoxModel doFillFrameworkIdItems(@QueryParameter APIDevice.OsType osType) {
        ListBoxModel frameworks = new ListBoxModel();
        frameworks.add(EMPTY_OPTION);
        if (osType != UNDEFINED) {
            try {
                APIUser user = TestdroidApiUtil.getGlobalApiClient().getUser();
                final Context<APIFramework> context = new Context<>(APIFramework.class, 0, MAX_VALUE, EMPTY, EMPTY);
                context.addFilter(new FilterEntry(OS_TYPE, EQ, osType.name()));
                context.addFilter(trueFilterEntry(FOR_PROJECTS));
                context.addFilter(trueFilterEntry(CAN_RUN_FROM_UI));
                final APIListResource<APIFramework> availableFrameworksResource = user
                        .getAvailableFrameworksResource(context);
                frameworks.addAll(availableFrameworksResource.getEntity().getData().stream()
                        .filter(f -> !APIFramework.Type.CLIENT_SIDE.name().equalsIgnoreCase(f.getType()))
                        .map(f -> new ListBoxModel.Option(f.getName(), f.getId().toString())).collect(toList()));
            } catch (APIException e) {
                LOGGER.log(Level.WARNING, Messages.ERROR_API());
            }
        }
        return frameworks;
    }

    @POST
    default FormValidation doCheckFrameworkId(@QueryParameter String value) {
        return parseLong(value).isPresent() ? FormValidation.ok() : FormValidation.error(DEFINE_FRAMEWORK());
    }

    default Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException nfe) {
            return Optional.empty();
        }
    }

}
