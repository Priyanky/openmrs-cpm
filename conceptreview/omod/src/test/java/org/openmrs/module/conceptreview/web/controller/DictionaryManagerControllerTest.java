package org.openmrs.module.conceptreview.web.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.conceptpropose.web.dto.SubmissionDto;
import org.openmrs.module.conceptpropose.web.dto.SubmissionResponseDto;
import org.openmrs.module.conceptpropose.SubmissionResponseStatus;
import org.openmrs.module.conceptreview.*;
import org.openmrs.module.conceptreview.api.ProposedConceptReviewService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DictionaryManagerController.class, Context.class})
public class DictionaryManagerControllerTest {
	
	@Mock
	private ConceptDatatype dataTypeMock;

	@Mock
	private ConceptClass conceptClassMock;

	@Mock
	private ConceptService conceptServiceMock;

	@Mock
	private ProposedConceptReviewService proposedConceptServiceMock;

	private DictionaryManagerController controller;

	@Before
	public void before() throws Exception {
		controller = new DictionaryManagerController();

		mockStatic(Context.class);
		when(Context.getConceptService()).thenReturn(conceptServiceMock);
		when(Context.getService(ProposedConceptReviewService.class)).thenReturn(proposedConceptServiceMock);
	}

	@Test
	public void submitProposal_regularProposal_shouldPersistDetails() throws Exception {
		final SubmissionDto dto = setupRegularProposalFixtureWithJson();
		setupRegularFixtureMocks();

		SubmissionResponseDto review = controller.submitProposal(dto);

		ArgumentCaptor<ProposedConceptReviewPackage> captor = ArgumentCaptor.forClass(ProposedConceptReviewPackage.class);
		verify(proposedConceptServiceMock).saveProposedConceptReviewPackage(captor.capture());
		final ProposedConceptReviewPackage value = captor.getValue();
		assertThat(value.getName(), is("A proposal"));
		assertThat(value.getEmail(), is("asdf@asdf.com"));
		assertThat(value.getDescription(), is("A description"));

		final ArrayList<ProposedConceptReview> proposedConcepts = new ArrayList<ProposedConceptReview>(value.getProposedConcepts());
		final ProposedConceptReview proposedConceptReview = proposedConcepts.get(0);
		assertThat(proposedConceptReview.getProposedConceptUuid(), is("concept-uuid"));
		assertThat(proposedConceptReview.getComment(), is("some comment"));
		assertThat(proposedConceptReview.getConceptClass(), is(conceptClassMock));
		assertThat(proposedConceptReview.getDatatype(), is(dataTypeMock));

		final List<ProposedConceptReviewName> names = proposedConceptReview.getNames();
		assertThat(names.size(), is(1));
		ProposedConceptReviewName name = names.get(0);
		assertThat(name.getName(), is("Concept name"));
		assertThat(name.getType(), is(ConceptNameType.FULLY_SPECIFIED));
		assertThat(name.getLocale(), is(Locale.ENGLISH));

		final List<ProposedConceptReviewDescription> descriptions = proposedConceptReview.getDescriptions();
		assertThat(descriptions.size(), is(1));
		ProposedConceptReviewDescription description = descriptions.get(0);
		assertThat(description.getDescription(), is("Concept description"));
		assertThat(description.getLocale(), is(Locale.ENGLISH));
		
		assertThat(review.getStatus(), is(SubmissionResponseStatus.SUCCESS));
		assertThat(review.getMessage(), is("All Good!"));
		assertThat(review.getId(), is(0));
	}

	private void setupRegularFixtureMocks() throws Exception {
		when(conceptServiceMock.getConceptDatatypeByUuid("datatype-uuid")).thenReturn(dataTypeMock);
		when(conceptServiceMock.getConceptClassByUuid("concept-class-uuid")).thenReturn(conceptClassMock);
		when(dataTypeMock.getUuid()).thenReturn("uuid!");
	}

	private SubmissionDto setupRegularProposalFixtureWithJson() throws Exception {

		String regularFixture =
				"{" +
				"  'name': 'A proposal'," +
				"  'email': 'asdf@asdf.com'," +
				"  'description': 'A description'," +
				"  'concepts': [" +
				"    {" +
				"      'uuid': 'concept-uuid'," +
				"      'conceptClass': 'concept-class-uuid'," +
				"      'datatype': 'datatype-uuid'," +
				"      'comment': 'some comment'," +
				"      'names': [" +
				"        {" +
				"          'name': 'Concept name'," +
				"          'type': 'FULLY_SPECIFIED'," +
				"          'locale': 'en'" +
				"        }" +
				"      ]," +
				"      'descriptions': [" +
				"        {" +
				"          'description': 'Concept description'," +
				"          'locale': 'en'" +
				"        }" +
				"      ]" +
				"    }" +
				"  ]" +
				"}";

		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(regularFixture.replace("'", "\""), SubmissionDto.class);
	}

	@Test
	public void submitProposal_numericProposal_shouldPersistDetails() throws Exception {
		final SubmissionDto dto = setupNumericProposalFixture();
		setupRegularFixtureMocks();
		setupNumericFixtureMocks();

		controller.submitProposal(dto);

		ArgumentCaptor<ProposedConceptReviewPackage> captor = ArgumentCaptor.forClass(ProposedConceptReviewPackage.class);
		verify(proposedConceptServiceMock).saveProposedConceptReviewPackage(captor.capture());
		final ProposedConceptReviewPackage value = captor.getValue();
		final ArrayList<ProposedConceptReview> proposedConcepts = new ArrayList<ProposedConceptReview>(value.getProposedConcepts());
		final ProposedConceptReview proposedConceptReview = proposedConcepts.get(0);
		assertThat(proposedConceptReview.getDatatype(), is(dataTypeMock));

		final ProposedConceptReviewNumeric numericDetails = proposedConceptReview.getNumericDetails();
		assertThat(numericDetails.getUnits(), is("ml"));
		assertThat(numericDetails.getPrecise(), is(true));
		assertThat(numericDetails.getHiNormal(), is(100.5));
		assertThat(numericDetails.getHiCritical(), is(110.0));
		assertThat(numericDetails.getHiAbsolute(), is(1000.0));
		assertThat(numericDetails.getLowNormal(), is(20.3));
		assertThat(numericDetails.getLowCritical(), is(15.0));
		assertThat(numericDetails.getLowAbsolute(), is(0.0));
	}

	private SubmissionDto setupNumericProposalFixture() throws IOException {
		final String fixture =
				"{" +
				"  'name': 'A proposal'," +
				"  'email': 'asdf@asdf.com'," +
				"  'description': 'A description'," +
				"  'concepts': [" +
				"    {" +
				"      'uuid': 'concept-uuid'," +
				"      'conceptClass': 'concept-class-uuid'," +
				"      'datatype': '8d4a4488-c2cc-11de-8d13-0010c6dffd0f'," +
				"      'comment': 'some comment'," +
				"      'names': [" +
				"        {" +
				"          'name': 'Concept name'," +
				"          'locale': 'en'" +
				"        }" +
				"      ]," +
				"      'descriptions': [" +
				"        {" +
				"          'description': 'Concept description'," +
				"          'locale': 'en'" +
				"        }" +
				"      ]," +
				"      'numericDetails': {" +
				"        'units': 'ml'," +
				"        'precise': true," +
				"        'hiNormal': 100.5," +
				"        'hiCritical': 110," +
				"        'hiAbsolute': 1000," +
				"        'lowNormal': 20.3," +
				"        'lowCritical': 15," +
				"        'lowAbsolute': 0" +
				"      }" +
				"    }" +
				"  ]" +
				"}";
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(fixture.replace("'", "\""), SubmissionDto.class);
	}

	private void setupNumericFixtureMocks() throws Exception {
		when(conceptServiceMock.getConceptDatatypeByUuid("8d4a4488-c2cc-11de-8d13-0010c6dffd0f")).thenReturn(dataTypeMock);
		when(dataTypeMock.getUuid()).thenReturn("8d4a4488-c2cc-11de-8d13-0010c6dffd0f");
	}
}