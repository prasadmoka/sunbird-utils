package org.sunbird.common.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.responsecode.ResponseCode;

/**
 * This util class for providing type safe config to any service that requires it.
 *
 * @author Manzarul
 */
public class ConfigUtil {

  private static Config config;
  private static final String DEFAULT_TYPE_SAFE_CONFIG_FILE_NAME = "service.conf";
  private static final String INVALID_FILE_NAME = "Please provide a valid file name.";

  /** Private default constructor. */
  private ConfigUtil() {}

  /**
   * This method will create a type safe config object and return to caller. It will read the config
   * value from System env first and as a fall back it will use service.conf file.
   *
   * @return Type safe config object
   */
  public static Config getConfig() {
    if (config == null) {
      synchronized (ConfigUtil.class) {
        config = createConfig(DEFAULT_TYPE_SAFE_CONFIG_FILE_NAME);
      }
    }
    return config;
  }

  /**
   * This method will create a type safe config object and return to caller. It will read the config
   * value from System env first and as a fall back it will use provided file name. If file name is
   * null or empty then it will throw ProjectCommonException with status code as 500.
   *
   * @return Type safe config object
   */
  public static Config getConfig(String fileName) {
    if (StringUtils.isBlank(fileName)) {
      ProjectLogger.log(
          "ConfigUtil:getConfigWithFilename: Given file name is null or empty: " + fileName,
          LoggerEnum.INFO.name());
      throw new ProjectCommonException(
          ResponseCode.internalError.getErrorCode(),
          INVALID_FILE_NAME,
          ResponseCode.CLIENT_ERROR.getResponseCode());
    }
    if (config == null) {
      synchronized (ConfigUtil.class) {
        config = createConfig(fileName);
      }
    }
    return config;
  }

  public static void validateMandatoryConfigValue(String configParameter) {
    if (StringUtils.isBlank(configParameter)) {
      ProjectLogger.log(
          "ConfigUtil:validateMandatoryConfigValue: Missing mandatory configuration parameter: "
              + configParameter,
          LoggerEnum.ERROR.name());
      throw new ProjectCommonException(
          ResponseCode.mandatoryConfigParamMissing.getErrorCode(),
          ResponseCode.mandatoryConfigParamMissing.getErrorMessage(),
          ResponseCode.SERVER_ERROR.getResponseCode(),
          configParameter);
    }
  }

  private static Config createConfig(String fileName) {
    Config defaultConf = ConfigFactory.load(fileName);
    Config envConf = ConfigFactory.systemEnvironment();
    return envConf.withFallback(defaultConf);
  }

  /*
   * @desc This method will parse json string and return config object
   * @param json string to be parsed
   * @return config object generated by parsing the json string
   */
  public static Config parseJSONString(String jsonString) {
    ProjectLogger.log(
        "ConfigUtil:parseJSONString: Parsing json string to config object", LoggerEnum.INFO.name());
    if (null == jsonString || StringUtils.isBlank(jsonString)) {
      ProjectLogger.log(
          "ConfigUtil:parseJSONString: Empty string is passed for parsing",
          LoggerEnum.ERROR.name());
      ProjectCommonException.throwServerErrorException(ResponseCode.errorConfigLoadEmptyString, "");
    }
    Config jsonConfig = null;
    try {
      jsonConfig = ConfigFactory.parseString(jsonString);
    } catch (Exception e) {
      ProjectLogger.log(
          "ConfigUtil:parseJSONString: Error while parsing json string to config object",
          LoggerEnum.ERROR.name());
      ProjectCommonException.throwServerErrorException(ResponseCode.errorConfigLoadParseString, "");
    }
    if (null == jsonConfig || jsonConfig.isEmpty()) {
      ProjectLogger.log(
          "ConfigUtil:parseJSONString: Config object is empty", LoggerEnum.ERROR.name());
      ProjectCommonException.throwServerErrorException(ResponseCode.errorConfigLoadEmptyConfig, "");
    }
    ProjectLogger.log(
        "ConfigUtil:parseJSONString: Json string is successfully parsed into a config object",
        LoggerEnum.INFO.name());
    return jsonConfig;
  }
}
