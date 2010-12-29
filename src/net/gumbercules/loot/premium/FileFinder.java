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
