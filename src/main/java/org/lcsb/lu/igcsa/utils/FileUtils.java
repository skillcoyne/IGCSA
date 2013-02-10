package org.lcsb.lu.igcsa.utils;

import org.lcsb.lu.igcsa.genome.Chromosome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Set;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FileUtils
  {




  public static File[] listFASTAFiles(File fastaDir) throws FileNotFoundException
    {
    FilenameFilter fastaFilter = new FilenameFilter()
      {
      public boolean accept(File dir, String name)
        {
        String lowercaseName = name.toLowerCase();
        if (lowercaseName.endsWith(".fa")) return true;
        else return false;
        }
      };
    return fastaDir.listFiles(fastaFilter);
    }

  }
