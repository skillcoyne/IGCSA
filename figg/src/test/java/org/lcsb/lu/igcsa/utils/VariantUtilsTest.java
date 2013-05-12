package org.lcsb.lu.igcsa.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.normal.SNVProbabilityDAO;
import org.lcsb.lu.igcsa.database.normal.SizeDAO;
import org.lcsb.lu.igcsa.database.normal.VariationDAO;
import org.lcsb.lu.igcsa.variation.fragment.Variation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;
/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class VariantUtilsTest
  {
  @Autowired
  private VariantUtils testVariantUtil;

  @Test
  public void testGetVariantList() throws Exception
    {
    assertNotNull(testVariantUtil);

    List<Variation> variationList = testVariantUtil.getVariantList("5");
    assertNotNull(variationList);
    assertEquals(variationList.size(), 3);
    }
  }
