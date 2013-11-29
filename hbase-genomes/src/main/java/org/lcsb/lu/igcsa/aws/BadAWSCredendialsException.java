/**
 * org.lcsb.lu.igcsa.aws
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

package org.lcsb.lu.igcsa.aws;

import com.amazonaws.auth.PropertiesCredentials;
import org.apache.log4j.Logger;

public class BadAWSCredendialsException extends RuntimeException
  {
  public static final BadAWSCredendialsException[] EMPTY_ARRAY = {};

  public static final String BAD_AMAZON_KEY = "Fill in your key";
  public static final String BAD_AMAZON_SECRET_KEY = "Fill in your secret key";

  public static void validateAWSCredentials(PropertiesCredentials props) throws BadAWSCredendialsException
    {
    if (props == null)
      throw new BadAWSCredendialsException();

    String access = props.getAWSAccessKeyId();
    if (access == null)
      throw new BadAWSCredendialsException();
    if (access.equals(BAD_AMAZON_KEY))
      throw new BadAWSCredendialsException();

    String secret = props.getAWSSecretKey();
    if (secret == null)
      throw new BadAWSCredendialsException();
    if (secret.equals(BAD_AMAZON_SECRET_KEY))
      throw new BadAWSCredendialsException();
    }

  public static final String BadAWSCredendialsExceptionMessage = "The resource AWSCredentials.properties must be modified to hold your" +
      "amazon credentials - google aws secret key than fill in the key and the " +
      "secret key from your account ";

  /**
   * Constructs a new runtime exception with <code>null</code> as its
   * detail message.  The cause is not initialized, and may subsequently be
   * initialized by a call to {@link #initCause}.
   */
  public BadAWSCredendialsException()
    {
    super(BadAWSCredendialsExceptionMessage);
    }


  }
