package net.gumbercules.loot.premium;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class SynchronizationProvider extends ContentProvider
{
	public static final Uri CONTENT_URI = Uri.parse("content://net.gumbercules.loot.synchronizationprovider");
	public static final String SYNC_ITEM_MIME = "vnd.android.cursor.item/vnd.gumbercules.synchronization";
	public static final String SYNC_DIR_MIME = "vnd.android.cursor.dir/vnd.gumbercules.synchronization";
	public static final String TIMESTAMP_ITEM_MIME = "vnd.android.cursor.item/vnd.gumbercules.long";

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2)
	{
		return 0;
	}

	@Override
	public String getType(Uri uri)
	{
		if (!uri.getScheme().equals("content"))
		{
			return null;
		}
		
		List<String> path = uri.getPathSegments();
		int size = path.size();
		
		if (size == 0)
		{
			return SYNC_DIR_MIME;
		}
		else if (size == 1)
		{
			return SYNC_DIR_MIME;
		}
		else if (size == 2)
		{
			if (path.get(1).equals("latest"))
			{
				return TIMESTAMP_ITEM_MIME;
			}
			else
			{
				return SYNC_ITEM_MIME;
			}
		}
		
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		if (getType(uri) != SYNC_DIR_MIME)
		{
			return null;
		}
		
		Synchronization sync = new Synchronization(values.getAsString("device_uuid"),
				values.getAsLong("timestamp"));
		if (sync.write())
		{
			if (uri.getPath() == null)
			{
				uri = Uri.withAppendedPath(uri, sync.uuid + '/' + sync.timestamp);
			}
			else
			{
				uri = Uri.withAppendedPath(uri, "/" + sync.timestamp);
			}
			
			return uri;
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean onCreate()
	{
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
	{
		String type = getType(uri);
		if (type.equals(TIMESTAMP_ITEM_MIME))
		{
			Synchronization sync = new Synchronization(uri.getPathSegments().get(0));
			sync.getLatest();
			MatrixCursor cur = new MatrixCursor(new String[] {"key", "value"});
			cur.addRow(new Object[] {"timestamp", new Long(sync.timestamp)});
			return cur;
		}
		
		// TODO: maybe provide values for SYNC_DIR_MIME and SYNC_ITEM_MIME
		
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs)
	{
		return 0;
	}
}
