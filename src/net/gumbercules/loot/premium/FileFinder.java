/*
 * This file is part of the loot project for Android.
 *
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. This program is distributed in the 
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. You should have received a copy of the GNU General 
 * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2008, 2009, 2010, 2011 Christopher McCurdy
 */

package net.gumbercules.loot.premium;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileFinder
{
	private ArrayList<String> mFilter;
	
	public FileFinder()
	{
		mFilter = new ArrayList<String>();
	}
	
	public FileFinder(String filter)
	{
		mFilter = new ArrayList<String>();
		mFilter.add(filter);
	}
	
	public FileFinder(String[] filter)
	{
		mFilter = new ArrayList<String>();
		mFilter.addAll(Arrays.asList(filter));
	}
	
	public FileFinder(List<String> filter)
	{
		mFilter = new ArrayList<String>();
		mFilter.addAll(filter);
	}
	
	public void addFilter(String filter)
	{
		mFilter.add(filter);
	}
	
	public void addFilters(String[] filter)
	{
		mFilter.addAll(Arrays.asList(filter));
	}
	
	public void addFilters(List<String> filter)
	{
		mFilter.addAll(filter);
	}
	
	public ArrayList<String> getFilter()
	{
		return mFilter;
	}
	
	public void findFiles(String top_level, ArrayList<String> files)
	{
		FileFilter ff = new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				for (String filter : mFilter)
				{
					if (pathname.getName().toLowerCase().contains(filter.toLowerCase()))
					{
						return true;
					}
				}
				
				return false;
			}
		};
		
		File file = new File(top_level);
		
		findFiles(file, ff, files);
	}
	
	private void findFiles(File file, FileFilter ff, ArrayList<String> files)
	{
		if (file.isDirectory())
		{
			File[] listed_files = file.listFiles(ff);
			if (listed_files != null)
			{
				for (File f : file.listFiles(ff))
				{
					files.add(f.getPath());
				}
			}
			
			listed_files = file.listFiles();
			if (listed_files != null)
			{
				for (File f : file.listFiles())
				{
					if (f.isDirectory())
					{
						findFiles(f, ff, files);
					}
				}
			}
		}
	}
}
