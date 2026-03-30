package de.mq.odesolver.support;

import de.mq.odesolver.support.OdeFunctionUtil.Language;

public interface  OdeFunctionUtilFactory {
	
	 public static OdeFunctionUtil newOdeFunctionUtil(final Language language) {
		 return new OdeFunctionUtilImpl(language);
	 }
	 
	 public static OdeFunctionUtil newOdeFunctionUtil(final Language language, final boolean resultIsVector) {
		 return new OdeFunctionUtilImpl(language,resultIsVector);
	 }
	 
	 public static OdeFunctionUtil newOdeFunctionUtil(final Language language, final String vectorName, final boolean resultIsVector) {
		 return new OdeFunctionUtilImpl(language, vectorName, resultIsVector);
	 }

}
