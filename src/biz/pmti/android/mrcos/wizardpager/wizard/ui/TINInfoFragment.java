package biz.pmti.android.mrcos.wizardpager.wizard.ui;

import org.droidparts.widget.ClearableEditText;

import dev.dworks.libs.awizard.model.PageFragmentCallbacks;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import biz.pmti.android.mrcos.wizardpager.wizard.model.TINInfoPage;
import biz.pmti.drawerwizardlibrary.R;

//import dev.dworks.libs.awizard.model.PageFragmentCallbacks;


public class TINInfoFragment extends Fragment{
	private static final String ARG_KEY = "key";
	
	
    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    private TINInfoPage mPage;
    private ClearableEditText mTINView;
    private ClearableEditText mNameView;
    private ClearableEditText mAddressView;
	
    public static TINInfoFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        TINInfoFragment fragment = new TINInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public TINInfoFragment(){
    	
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Bundle args = getArguments();
    	mKey = args.getString(ARG_KEY);
    	mPage = (TINInfoPage) mCallbacks.onGetPage(mKey);
    	
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.fragment_page_tin_info, container, false);
    	((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());
    	
    	
    	mTINView = (ClearableEditText) rootView.findViewById(R.id.editText_Search);
    	mTINView.setText(mPage.getData().getString(TINInfoPage.TIN_DATA_KEY));
    	
    	mNameView = (ClearableEditText) rootView.findViewById(R.id.editText_Registered_Name);
    	mNameView.setText(mPage.getData().getString(TINInfoPage.NAME_DATA_KEY));
    	
    	mAddressView = (ClearableEditText) rootView.findViewById(R.id.editText_Address1);
    	mAddressView.setText(mPage.getData().getString(TINInfoPage.ADDRESS_DATA_KEY));
    	
    	return rootView;
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	
        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }
        
        mCallbacks = (PageFragmentCallbacks) activity;
    }
    
    @Override
    public void onDetach() {
    	super.onDetach();
    	mCallbacks = null;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	
    	  mTINView.addTextChangedListener(new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                      int i2) {
              }

              @Override
              public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
              }

              @Override
              public void afterTextChanged(Editable editable) {
                  mPage.getData().putString(TINInfoPage.TIN_DATA_KEY,
                          (editable != null) ? editable.toString() : null);
                  mPage.notifyDataChanged();
              }
          });
    	  
    	  mAddressView.addTextChangedListener(new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                      int i2) {
              }

              @Override
              public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
              }

              @Override
              public void afterTextChanged(Editable editable) {
                  mPage.getData().putString(TINInfoPage.ADDRESS_DATA_KEY,
                          (editable != null) ? editable.toString() : null);
                  mPage.notifyDataChanged();
              }
          });
    	  
    	  mNameView.addTextChangedListener(new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                      int i2) {
              }

              @Override
              public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
              }

              @Override
              public void afterTextChanged(Editable editable) {
                  mPage.getData().putString(TINInfoPage.NAME_DATA_KEY,
                          (editable != null) ? editable.toString() : null);
                  mPage.notifyDataChanged();
              }
          });
    	  
    	  
    	  
    }
    @Override
    public void setMenuVisibility(boolean menuVisible) {//not sure of the purpose alvin
    	super.setMenuVisibility(menuVisible);
    	   // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mNameView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}
