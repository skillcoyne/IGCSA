/**
 * org.lcsb.lu.igcsa.aws
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AWSUtils
  {
  static Logger log = Logger.getLogger(AWSUtils.class.getName());

  private static AmazonS3 s3;

  /**
   * In any bucket the fasta files must be contained in a folder (or "prefix") named 'FASTA'
   * @param bucket
   * @return
   */
  public static Map<String, S3ObjectSummary> listFASTAFiles(String bucket)
    {
    Map<String, S3ObjectSummary> objects = new HashMap<String, S3ObjectSummary>();
    AWSCredentials creds = AWSUtils.getCredentials();
    AmazonS3 s3 = new AmazonS3Client(creds);
    //AmazonS3 s3 = new AmazonS3Client();
    ObjectListing listing = s3.listObjects(new ListObjectsRequest().withBucketName(bucket).withPrefix("FASTA"));

    Pattern p = Pattern.compile("^FASTA\\/chr(\\d+|X|Y)\\.fa.*");
    for (S3ObjectSummary summary : listing.getObjectSummaries())
      {
      Matcher match = p.matcher(summary.getKey());
      if (match.matches())
        {
        String chr = match.toMatchResult().group(1);
        objects.put(chr, summary);
        }
      }
    return objects;
    }

  public static S3Object getS3Object(S3ObjectSummary summary)
    {
    AmazonS3 s3 = getS3();
    GetObjectRequest request = new GetObjectRequest(summary.getBucketName(), summary.getKey());
    return s3.getObject(request);
    }

  public static InputStream openFileS3(String bucket, String file)
    {
    AmazonS3 s3 = getS3();
    System.err.println("reading s3 bucket " + bucket + " key " + file);
    try
      {
      S3Object object = s3.getObject(bucket, file);
      InputStream objectContent = object.getObjectContent();
      return objectContent;
      }
    catch (AmazonClientException ex)
      {
      throw new RuntimeException(ex);
      }

    }

  public static AWSCredentials getCredentials()
    {
    String key = AWSProperties.getProperties().getAccessKey();
    String secretKey = AWSProperties.getProperties().getSecretKey();

    if (key == null || secretKey == null)
      throw new BadAWSCredendialsException();
    AWSCredentials credentials = new BasicAWSCredentials(key, secretKey);
    return credentials;
    }


  public static String[] listFiles(String bucketName, String directoryName) throws IllegalArgumentException
    {
    AmazonS3 s3 = getS3();
    try
      {
      ListObjectsRequest lo = new ListObjectsRequest();
      lo.setBucketName(bucketName);
      lo.setPrefix(directoryName);
      lo.setMaxKeys(Integer.MAX_VALUE);
      final ObjectListing objectListing = s3.listObjects(lo);
      List<String> holder = new ArrayList<String>();

      final List<S3ObjectSummary> summaryList = objectListing.getObjectSummaries();
      for (S3ObjectSummary os : summaryList)
        {
        final String key = os.getKey();
        holder.add(key);
        }
      String[] ret = new String[holder.size()];
      holder.toArray(ret);
      return ret;
      }
    catch (AmazonClientException ex)
      {
      throw new RuntimeException(ex);
      }
    }


  public synchronized static AmazonS3 getS3()
    {
    try
      {
      if (s3 == null)
        {
        AWSCredentials credentials = getCredentials();
        s3 = new AmazonS3Client(credentials);
        }
      return s3;
      }
    catch (AmazonClientException ex)
      {
      throw new RuntimeException(ex);
      }

    }


  }
