package de.mq.odesolver.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class BasicMockitoControllerTest {

	private final OdeSessionModelRepository odeSessionModelRepository = Mockito.mock(OdeSessionModelRepository.class);
	private final MessageSource messageSource = Mockito.mock(MessageSource.class);
	private final BindingResult bindingResult = Mockito.mock(BindingResult.class);
	private final Model model = Mockito.mock(Model.class);
	private final Map<String, Object> attributes = new HashMap<>();
	private final OdeSessionModel odeSessionModel = new OdeSessionModel();
	private final List<ObjectError> globalErrors = new ArrayList<>();
	private final Locale locale = Locale.GERMAN;

	@BeforeEach
	void setup() {
		
		Mockito.when(odeSessionModelRepository.odeSessionModel()).thenReturn(odeSessionModel);
		Mockito.doAnswer(a -> a.getArguments()[0]).when(messageSource).getMessage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doAnswer(a -> attributes.put(a.getArgument(0, String.class), a.getArgument(1))).when(model).addAttribute(Mockito.anyString(), Mockito.any());
		Mockito.doAnswer(a -> globalErrors.add(a.getArgument(0))).when(bindingResult).addError(Mockito.any(ObjectError.class));
		Mockito.doAnswer(_ -> !globalErrors.isEmpty()).when(bindingResult).hasGlobalErrors();
		Mockito.doAnswer(_ -> !globalErrors.isEmpty()).when(bindingResult).hasErrors();
	}

	protected BindingResult bindingResult() {
		return bindingResult;
	}

	protected Model model() {
		return model;
	}

	protected Map<String, Object> attributes() {
		return Collections.unmodifiableMap(attributes);
	}

	protected OdeSessionModel odeSessionModel() {
		return odeSessionModel;
	}

	protected List<ObjectError> globalErrors() {
		return Collections.unmodifiableList(globalErrors);
	}

	protected MessageSource messageSource() {
		return messageSource;
	}

	protected OdeSessionModelRepository odeSessionModelRepository() {
		return odeSessionModelRepository;
	}

	protected Locale locale() {
		return locale;
	}
}
