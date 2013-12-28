package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.tables
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class LocationTest
  {
  static Logger log = Logger.getLogger(LocationTest.class.getName());

  private Location location;

  @Before
  public void setUp() throws Exception
    {
    location = new Location(5, 35);
    }

  @Test
  public void testGetStart() throws Exception
    {
    assertEquals(location.getStart(), 5);
    }

  @Test
  public void testGetEnd() throws Exception
    {
    assertEquals(location.getEnd(), 35);
    }

  @Test
  public void testGetLength() throws Exception
    {
    assertEquals(location.getLength(), 35-5);
    }

  @Test
  public void testEquals() throws Exception
    {
    assertEquals(location, new Location(5,35));
    assertFalse( location.equals( new Location(5,22) ) );
    }

  @Test
  public void testOverlaps() throws Exception
    {
    assertTrue( location.overlapsLocation( new Location(12, 42) ) );
    assertFalse(location.overlapsLocation( new Location(1, 4) ) );
    }

  @Test
  public void testContains() throws Exception
    {
    assertTrue(location.containsLocation(new Location(12, 22)) );
    assertFalse(location.containsLocation(new Location(22, 40) ));
    }

  }
