package io.mosip.preregistration.application.test.service.util;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.mosip.preregistration.application.exception.OperationNotAllowedException;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.service.AppointmentService;
import io.mosip.preregistration.application.service.UISpecService;
import io.mosip.preregistration.application.service.util.DemographicServiceUtil;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import io.mosip.preregistration.demographic.dto.DemographicRequestDTO;
import io.mosip.preregistration.demographic.exception.system.DateParseException;
import io.mosip.preregistration.demographic.exception.system.JsonParseException;

/**
 * Test class to test the PreRegistration Service util methods
 * 
 * @author Ravi C Balaji
 * @since 1.0.0
 * 
 */
@RunWith(SpringRunner.class)
@ImportAutoConfiguration(RefreshAutoConfiguration.class)
@SpringBootTest(classes = DemographicServiceUtil.class)
public class DemographicServiceUtilTest {

	/**
	 * Autowired reference for $link{DemographicServiceUtil}
	 */
	@Autowired
	private DemographicServiceUtil demographicServiceUtil;

	@MockBean(name = "selfTokenRestTemplate")
	RestTemplate restTemplate;

	@MockBean
	private AppointmentService appointmentService;

	@MockBean
	private ApplicationRepostiory applicationRepostiory;

	@MockBean
	private RequestValidator requestValidator;
	
	@MockBean
	private UISpecService uiSpecService;

	private DemographicRequestDTO saveDemographicRequest = null;
	private DemographicRequestDTO updateDemographicRequest = null;
	private DemographicEntity demographicEntity = null;
	private String requestId = null;
	private JSONObject jsonObject;
	private JSONParser parser = null;

	@MockBean
	private AuditLogUtil auditLogUtil;

	@MockBean
	private CryptoUtil cryptoUtil;

	/**
	 * @throws Exception on Any Exception
	 */
	@Before
	public void setUp() throws Exception {
		requestId = "mosip.preregistration";
		parser = new JSONParser();

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("pre-registration.json").getFile());
		jsonObject = (JSONObject) parser.parse(new FileReader(file));

		saveDemographicRequest = new DemographicRequestDTO();
		saveDemographicRequest.setLangCode("ENG");
		saveDemographicRequest.setDemographicDetails(jsonObject);

		updateDemographicRequest = new DemographicRequestDTO();
		updateDemographicRequest.setLangCode("ENG");
		updateDemographicRequest.setDemographicDetails(jsonObject);

		demographicEntity = new DemographicEntity();
		demographicEntity.setPreRegistrationId("35760478648170");
		demographicEntity.setApplicantDetailJson((jsonObject.toJSONString() + "623744").getBytes());
	}

	@Test(expected = JsonParseException.class)
	public void setterForCreateDTOFailureTest() {
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any()))
				.thenReturn(Base64.decodeBase64(jsonObject.toString().getBytes()));
		Mockito.when(demographicServiceUtil.setterForCreateDTO(demographicEntity)).thenThrow(JsonParseException.class);
	}

	@Test(expected = OperationNotAllowedException.class)
	public void checkStatusForDeletionFailureTest() {
		Mockito.when(demographicServiceUtil.checkStatusForDeletion(StatusCodes.EXPIRED.getCode()))
				.thenThrow(OperationNotAllowedException.class);
	}

	@Test(expected = DateParseException.class)
	public void getDateFromStringFailureTest() throws Exception {
		demographicServiceUtil.getDateFromString("abc");
	}

}
