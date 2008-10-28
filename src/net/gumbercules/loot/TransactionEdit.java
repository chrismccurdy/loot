package net.gumbercules.loot;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableRow;

public class TransactionEdit extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trans);

		// get the request code so we know whether to show a transaction or a transfer window
		Bundle extras = getIntent().getExtras();
		int request = extras.getInt("REQUEST");
		
		// if we're showing a transfer window, hide the check button, check field, and party field
		if (request == Loot.ACTIVITY_TRANSFER_CREATE)
		{
			RadioButton checkButton = (RadioButton)findViewById(R.id.checkRadio);
			checkButton.setVisibility(RadioButton.GONE);
			
			TableRow row = (TableRow)findViewById(R.id.partyRow);
			row.setVisibility(TableRow.GONE);
			row = (TableRow)findViewById(R.id.checkRow);
			row.setVisibility(TableRow.GONE);
			row = (TableRow)findViewById(R.id.accountRow);
			row.setVisibility(TableRow.VISIBLE);
		}
		else
		{
			// set the check radio to enable/disable and automatically populate the check entry field
			RadioButton checkButton = (RadioButton)findViewById(R.id.checkRadio);
			checkButton.setOnCheckedChangeListener( new RadioButton.OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					EditText checkEdit = (EditText)findViewById(R.id.checkEdit);
					if (isChecked)
					{
						if (checkEdit.getText().toString() == "")
						{
							// TODO: autopopulate the edit with the next check number
						}
					}
					checkEdit.setEnabled(isChecked);
				}
			});
		}
	}
}
