package org.openmrs.module.coreapps.htmlformentry;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.api.ConceptService;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.DispositionType;
import org.openmrs.module.htmlformentry.BadFormDesignException;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.module.htmlformentry.FormSubmissionController;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EncounterDiagnosesTagHandlerTest {

    private EncounterDiagnosesTagHandler encounterDiagnosesTagHandler;

    private FormEntrySession formEntrySession;

    private FormEntryContext formEntryContext;

    private UiUtils uiUtils;

    private FormSubmissionController formSubmissionController;

    private ConceptService conceptService;

    private AdtService adtService;

    private EmrApiProperties emrApiProperties;

    @Before
    public void setup() {
        formEntrySession = mock(FormEntrySession.class);
        formEntryContext = mock(FormEntryContext.class);
        uiUtils = mock(UiUtils.class);        
        formSubmissionController = mock(FormSubmissionController.class);
        conceptService = mock(ConceptService.class);
        adtService = mock(AdtService.class);
        emrApiProperties = mock(EmrApiProperties.class);

        when(formEntrySession.getContext()).thenReturn(formEntryContext);
        
        encounterDiagnosesTagHandler = new EncounterDiagnosesTagHandler();
        encounterDiagnosesTagHandler.setConceptService(conceptService);
        encounterDiagnosesTagHandler.setAdtService(adtService);
        encounterDiagnosesTagHandler.setEmrApiProperties(emrApiProperties);
        encounterDiagnosesTagHandler.setUiUtils(uiUtils);
    }

    @Test
    public void getSubstitution_shouldAddEncounterDiagnosisElementToFormSubmissionController() throws BadFormDesignException {

        when(formEntryContext.getMode()).thenReturn(FormEntryContext.Mode.ENTER);

        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "123");

        EncounterDiagnosesElement expectedEncounterDiagnosesElement = new EncounterDiagnosesElement();
        expectedEncounterDiagnosesElement.setDispositionTypeForPriorDiagnoses(DispositionType.ADMIT);
        expectedEncounterDiagnosesElement.setSelectedDiagnosesTarget("123");
        expectedEncounterDiagnosesElement.setRequired(true);

        encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);

        verify(formSubmissionController).addAction(argThat(new IsEncounterDiagnosesElementMatcher(expectedEncounterDiagnosesElement)));

    }

    @Test
    public void getSubstitution_shouldAddHiddenFieldCarryingConceptSourceName() throws Exception {
        // setup
        when(formEntryContext.getMode()).thenReturn(FormEntryContext.Mode.ENTER);
        when(uiUtils.includeFragment(eq("coreapps"), eq("diagnosis/encounterDiagnoses"), anyMap())).thenReturn("Some Fragment");
        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");
        attributes.put("conceptSource", "ICPC2");
        String hiddenInputElement = "<input type=\"hidden\" id=\"concept-source\" value=\"ICPC2\"/>";

        // replay
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);
    
        // verify
        assertTrue(StringUtils.contains(generatedHtml, hiddenInputElement));

    }

    @Test
    public void getSubstitution_shouldNotAddHiddenConceptSourceNameFieldGivenEmptyOrNullValue() throws Exception {
        // setup
        when(formEntryContext.getMode()).thenReturn(FormEntryContext.Mode.ENTER);
        when(uiUtils.includeFragment(eq("coreapps"), eq("diagnosis/encounterDiagnoses"), anyMap())).thenReturn("Some Fragment");
        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");
        attributes.put("conceptSource", "");
        String hiddenInputElementCodePiece = "\n<input type=\"hidden\" id=\"concept-source\"";

        // replay
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);
        
        // verify
        assertFalse(StringUtils.contains(generatedHtml, hiddenInputElementCodePiece));

    }

    @Test(expected = BadFormDesignException.class)
    public void getSubstitution_shouldAFailIfInvalidValueForPriorDiagnosesAttributeValueInvalid() throws BadFormDesignException {

        when(formEntryContext.getMode()).thenReturn(FormEntryContext.Mode.ENTER);

        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "invalid value");

        encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);
    }


    public class IsEncounterDiagnosesElementMatcher extends ArgumentMatcher<EncounterDiagnosesElement> {

        private EncounterDiagnosesElement expectedEncounterDiagnosesElement;

        public IsEncounterDiagnosesElementMatcher(EncounterDiagnosesElement encounterDiagnosesElement) {
            expectedEncounterDiagnosesElement = encounterDiagnosesElement;
        }

        @Override
        public boolean matches(Object o) {

            EncounterDiagnosesElement actualEncounterDiagnosesElement = (EncounterDiagnosesElement) o;

            boolean matches = true;

            matches = matches && actualEncounterDiagnosesElement.getSelectedDiagnosesTarget().equals(expectedEncounterDiagnosesElement.getSelectedDiagnosesTarget());
            matches = matches && actualEncounterDiagnosesElement.getDispositionTypeForPriorDiagnoses().equals(expectedEncounterDiagnosesElement.getDispositionTypeForPriorDiagnoses());
            matches = matches && actualEncounterDiagnosesElement.getRequired() == expectedEncounterDiagnosesElement.getRequired();

            return  matches;
        }
    }
}
