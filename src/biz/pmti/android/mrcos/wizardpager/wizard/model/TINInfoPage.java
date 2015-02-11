package biz.pmti.android.mrcos.wizardpager.wizard.model;

import java.util.ArrayList;

import biz.pmti.android.mrcos.wizardpager.wizard.ui.TINInfoFragment;
import dev.dworks.libs.awizard.model.ReviewItem;
import dev.dworks.libs.awizard.model.WizardModelCallbacks;
import dev.dworks.libs.awizard.model.page.Page;
import android.support.v4.app.Fragment;
import android.text.TextUtils;



public class TINInfoPage extends Page{
	
	/**
	 * 
	 * we can declare here the edittext/textview layout for retrieving data..
	 * all fields of the form declare here for retrieving the tags..
	**/
	public static final String TIN_DATA_KEY = "tin";
	public static final String NAME_DATA_KEY = "name";
	public static final String ADDRESS_DATA_KEY = "address";  
	
	
	public TINInfoPage(WizardModelCallbacks callbacks, String title) {
		super(callbacks, title);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment createFragment() {
		return TINInfoFragment.create(getKey());
	}

	@Override
	public void getReviewItems(ArrayList<ReviewItem> dest) {
		  dest.add(new ReviewItem("Your TIN", mData.getString(TIN_DATA_KEY), getKey(), -1));
		  dest.add(new ReviewItem("Your NAME", mData.getString(NAME_DATA_KEY), getKey(), -1));
		  dest.add(new ReviewItem("Your ADDRESS", mData.getString(ADDRESS_DATA_KEY), getKey(), -1));
	      
		
	}
	@Override
	public boolean isCompleted() {
		return !TextUtils.isEmpty(mData.getString(TIN_DATA_KEY))
				&& !TextUtils.isEmpty(mData.getString(NAME_DATA_KEY))
				&& !TextUtils.isEmpty(mData.getString(ADDRESS_DATA_KEY));// validate the
																// field if it's
																// empty
	}

/*	@Override
	public void getReviewItems(
			ArrayList<com.pmti.mrcos.wizardpager.wizard.model.ReviewItem> dest) {
		// TODO Auto-generated method stub
		
	}*/

}
