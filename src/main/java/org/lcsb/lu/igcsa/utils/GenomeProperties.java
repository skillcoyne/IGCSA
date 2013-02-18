package org.lcsb.lu.igcsa.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GenomeProperties extends Properties
  {
  public enum GenomeType
    {
      NORMAL("normal"), CANCER("cancer");

    private String name;

    private GenomeType(String s)
      {
      name = s;
      }

    public String getName()
      {
      return name;
      }
    }

  //private GenomeType type;

  private Map<String, GenomeProperties> variationProps = new HashMap<String, GenomeProperties>();


  public static GenomeProperties readPropertiesFile(String fileName, GenomeType type) throws IOException
    {
    GenomeProperties props = readPropertiesFile(fileName);
    props.loadVariationProperties(type);
    props.checkDefaultValues();
    return props;
    }

  public static GenomeProperties readPropertiesFile(String fileName) throws IOException
    {
    GenomeProperties props = new GenomeProperties();
    ClassLoader cl = ClassLoader.getSystemClassLoader(); // not sure this is the best way to do it, but it works

    try { props.load(cl.getResourceAsStream(fileName)); }
    catch (NullPointerException npe) { throw new FileNotFoundException(fileName + " not found"); }
    return props;
    }


  public GenomeProperties()
    { super(); }

  /**
   * Returns a new Properties object containing only properties with the given prefix.
   * The prefix string is removed from the keys.
   * @param prefix
   * @return Properties
   */
  public GenomeProperties getPropertySet(String prefix)
    {
    GenomeProperties p = new GenomeProperties();
    for (Object k : this.keySet())
      {
      String key = k.toString();
      if (key.startsWith(prefix + "."))
        {
        String newKey = key.replace(prefix + ".", "");
        p.setProperty(newKey, this.getProperty(key));
        }
      }
    return p;
    }


  /**
   * Get namespaced property, this will only be the properties named by the variation.normal or variation.cancer properties.
   * @param key
   * @return
   */
  public GenomeProperties getVariationProperty(String key)
    {
    return this.variationProps.get(key);
    }


  /**
   * Loads all property files in the named variation folder.
   * @throws IOException
   */
  public void loadVariationProperties(GenomeType type) throws IOException
    {
    if (this.getProperty("variation." + type.getName()) != null )
      {
      String variationProperty = this.getProperty("variation." + type.getName());
      String[] variationList = variationProperty.split(";");

      this.setProperty("variations", variationProperty);

      for (String var : variationList)
        {
        String fileName = type.getName() + File.separator + var + ".properties";
        try
          {
          GenomeProperties varProp = readPropertiesFile(fileName);
          varProp.checkVariationDefaults();
          this.variationProps.put(var, varProp);
          }
//        catch (FileNotFoundException fne)
//          { // There may not be a properties file defined for one of the variations.
//          System.out.println(fne.getMessage());
//          }
        catch (IOException ioe)
          {
          throw new IOException(type.getName() + "/" + var + ".properties file. " + ioe.getMessage());
          }
        }
      }
    }

  protected void checkVariationDefaults() throws IOException
    {
    if (!this.containsKey("freq")) throw new IOException("Missing required property: freq");
    }

  // I'd presume Spring is the best way to do this stuff but this is fine for now
  protected void checkDefaultValues() throws IOException
    {
    if (!this.containsKey("dir.assembly") || !this.containsKey("dir.insilico")) throw new IOException("Missing required properties: dir.assmebly or dir.insilico");
    if (!this.containsKey("variation.normal") || !this.containsKey("variation.cancer")) throw new IOException("Missing required properties: variation.normal or variation.cancer");

    if (!this.containsKey("generations")) this.setProperty("generations", "5");
    if (!this.containsKey("assembly"))  this.setProperty("assembly", "unknown");
    if (!this.containsKey("window")) this.setProperty("window", "100");
    if (!this.containsKey("kmer")) this.setProperty("kmer", "2");
    if (!this.containsKey("noise")) this.setProperty("noise", "100");
    if (!this.containsKey("zygosity")) this.setProperty("zygosity", "0.7");
    if (!this.containsKey("deviation")) this.setProperty("deviation", "0.8");
    }

  }
