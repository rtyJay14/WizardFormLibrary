/*
 * Copyright 2015 Rudson Lima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.liveo.navigationliveo;

import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import biz.pmti.drawerwizardlibrary.R;
//import com.example.liveodrawer.R;
import br.liveo.adapter.NavigationLiveoAdapter;
import br.liveo.interfaces.NavigationLiveoListener;
import dev.dworks.libs.awizard.model.PageFragmentCallbacks;
import dev.dworks.libs.awizard.model.ReviewCallbacks;
import dev.dworks.libs.awizard.model.WizardModel;
import dev.dworks.libs.awizard.model.WizardModelCallbacks;
import dev.dworks.libs.awizard.model.page.DonePage;
import dev.dworks.libs.awizard.model.page.Page;
import dev.dworks.libs.awizard.model.page.ReviewPage;
import dev.dworks.libs.awizard.model.ui.StepPagerStrip;

public abstract class NavigationLiveoAWizard extends ActionBarActivity
		implements PageFragmentCallbacks, ReviewCallbacks, WizardModelCallbacks {// FragmentActivity ActionBarActivity Activity
	
	
	/**
	 *  HaKr AWizard
	 * */
	
	public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

	//The view pager for the wizard
    private ViewPager mPager;
    //FragmentStatePagerAdapter adapter for the wizard
    private PagerAdapter mPagerAdapter;

    //Set to true to edit data at review
    private boolean mEditingAfterReview;
    
    private String mReviewText = null;
    private String mDoneText = null;
    
	//The wizard model for the wizard
    public WizardModel mWizardModel;

    private boolean mConsumePageSelectedEvent;

    //Next button in the wizard
    private Button mNextButton;
    //Previous button in the wizard
    private ImageButton mPrevButton;
    private ImageButton exit;

    private List<Page> mCurrentPageSequence;
    private StepPagerStrip mStepPagerStrip;
	private int mReviewPagePosition;
	private int mDonePagePosition;
	private int mOrientation = HORIZONTAL;
	private boolean mDataChanged = false;
	
	
	
	

    /*
     * Adapter for fragments 
     * */
    private class PagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
/*            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }
*/
            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

		@Override
		public int getCount() {
			if (mCurrentPageSequence == null) {
				return 0;
			}
			return Math.min(mCutOffPage + 1, mCurrentPageSequence.size() /* + 1 */);
		}

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }
	
	
	
	

    private void ensureControls() {
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        if (mPager == null) {
            throw new RuntimeException(
                    "Your content must have a android.support.v4.view.ViewPager whose id attribute is " +
                    "'R.id.pager'");
        }
        //mPager.setOrientation(mOrientation);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });
        
        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip.setOrientation(mOrientation);
        mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(mPagerAdapter.getCount() - 1, position);
                if (mPager.getCurrentItem() != position) {
                    mPager.setCurrentItem(position);
                }
            }
        });
        
        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPager.getCurrentItem() == mCurrentPageSequence.size() - 1) {
                	onDoneClick();
                } else {
                    if (mEditingAfterReview) {
                        mPager.setCurrentItem(mReviewPagePosition);
                    } else {
                    	if(mPager.getCurrentItem() == mReviewPagePosition){
                    		Page page = mCurrentPageSequence.get(mReviewPagePosition);
                    		page.getData().putBoolean(ReviewPage.PROCESS_DATA_KEY, true);
                    		page.notifyDataChanged();
                    	}
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    }
                }
            }
        });
        
        mPrevButton = (ImageButton) findViewById(R.id.prev);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });
        
        exit = (ImageButton) findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        
        mDonePagePosition = getDonePagePosition();
        mReviewPagePosition = getReviewPagePosition();
        mStepPagerStrip.setReviewPagePosition(mReviewPagePosition);
        mStepPagerStrip.setDonePagePosition(mDonePagePosition);;
	}
    
	/**
	 * @param showReview the show review to set
	 */
	public final void setReviewText(String finish) {
		this.mReviewText = finish;
	}
	

	/**
	 * @param showReview the show review to set
	 */
	public final void setDoneText(String finish) {
		this.mDoneText = finish;
	}
	
	/**
	 * on Review next action
	 */
	public void onConfirmClick() {
		finish();
	}
	
	/**
	 * on Done next action
	 */
	public void onDoneClick() {
		finish();
	}
    
    @Override
    public final void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        updatePagerStrip();
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void updatePagerStrip() {
    	int pageCount = mCurrentPageSequence.size();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(pageCount);
        mReviewPagePosition = getReviewPagePosition();
        mDonePagePosition = getDonePagePosition();
        mStepPagerStrip.setReviewPagePosition(mReviewPagePosition);
        mStepPagerStrip.setDonePagePosition(mDonePagePosition);;
	}

	private void updateBottomBar() {
    	if(null == mNextButton || null == mPrevButton){
    		return;
    	}
        TypedValue v = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
        int position = mPager.getCurrentItem();
        if (position == mReviewPagePosition) {
        	mReviewText = mReviewText != null ? mReviewText : getResources().getString(R.string.review_next);
            mNextButton.setText(mReviewText);
            mNextButton.setBackgroundResource(R.drawable.review_backgrounds);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
        }else if (position == mDonePagePosition) {
        	mDoneText = mDoneText != null ? mDoneText : getResources().getString(R.string.done_next);
            mNextButton.setText(mDoneText);
            mNextButton.setBackgroundResource(R.drawable.done_backgrounds);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
        }else if (position == mCurrentPageSequence.size() - 1) {
            mNextButton.setText(R.string.done_next);
            if(mDonePagePosition != -1){
                mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
                mNextButton.setTextAppearance(this, v.resourceId);
            }
            else{
            	mNextButton.setBackgroundResource(R.drawable.done_backgrounds);
            	mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
            }
        }else {
            mNextButton.setText(mEditingAfterReview ? R.string.review : R.string.next);
            mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
            mNextButton.setTextAppearance(this, v.resourceId);
//            mPrevButton.setTextAppearance(this, v.resourceId);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
        }

        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
    }

/*    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());
        outState.putBoolean("dataChanged", mDataChanged);
    }
*/
    @Override
    public final WizardModel getWizardModel() {
        return mWizardModel;
    }
    
    public final void setWizardModel(WizardModel wizardModel) {
    	mWizardModel = wizardModel;
        if (mWizardModel == null) {
            throw new RuntimeException("Wizard Model cannot be empty");
        }

        mWizardModel.registerListener(this);
        //TODO: Change in support lib causing null pointer
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        
        ensureControls();
        onPageTreeChanged();
    }
    
    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        switch (orientation) {
            case HORIZONTAL:
            case VERTICAL:
                break;

            default:
                throw new IllegalArgumentException("Only HORIZONTAL and VERTICAL are valid orientations.");
        }
        mOrientation = orientation;
        if(null != mStepPagerStrip){
        	mStepPagerStrip.setOrientation(mOrientation);
        }
    }

    @Override
    public final void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    @Override
    public final void onPageDataChanged(Page page) {
    	mDataChanged = true;
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }
    
    public boolean getDataChanged() {
        return mDataChanged;
    }

    private final int getReviewPagePosition() {
    	int pagePosition = -1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page instanceof ReviewPage) {
            	pagePosition = i;
                break;
            }
        }
        return pagePosition;
	}
    
    private final int getDonePagePosition() {
    	int pagePosition = -1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page instanceof DonePage) {
            	pagePosition = i;
                break;
            }
        }
        return pagePosition;
	}

    private final boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*************************************************************************************************/
	
	
	
	
	
	
	
	
	
	/**
	 * LIVEO MATERIAL DESIGN VARIABLES
	 * */

    public TextView mUserName;
    public TextView mUserEmail;
    public ImageView mUserPhoto;
    public ImageView mUserBackground;

    private ListView mList;
    private Toolbar mToolbar;

    private View mHeader;

    private TextView mTitleFooter;
    private ImageView mIconFooter;

    private int mColorDefault = 0;
    private int mColorSelected = 0;
    private int mCurrentPosition = 1;
    private int mNewSelector = 0;
    private boolean mRemoveAlpha = false;
    private boolean mRemoveSelector = false;

    private List<Integer> mListIcon;
    private List<Integer> mListHeader;
    private List<String> mListNameItem;
    private SparseIntArray mSparseCounter;

    private DrawerLayout mDrawerLayout;
    private FrameLayout mRelativeDrawer;
    private RelativeLayout mFooterDrawer;

    private NavigationLiveoAdapter mNavigationAdapter;
    private ActionBarDrawerToggleCompat mDrawerToggle;
    private NavigationLiveoListener mNavigationListener;

  //  public WizardActivity wizAct;
    
    public static final String CURRENT_POSITION = "CURRENT_POSITION";

    /**
     * User information
     */
    public abstract void onUserInformation();

    /**
     * onCreate(Bundle savedInstanceState).
     * @param savedInstanceState onCreate(Bundle savedInstanceState).
     */
    public abstract void onInt(Bundle savedInstanceState);


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navigation_main);

        if (savedInstanceState != null) {
            setCurrentPosition(savedInstanceState.getInt(CURRENT_POSITION));
            
            //FORM WIZARD
            mWizardModel.load(savedInstanceState.getBundle("model"));
            mDataChanged = savedInstanceState.getBoolean("dataChanged");
        }

        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(new DrawerItemClickListener());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
       // mToolbar.inflateMenu(R.menu.menu); ALVIN TEST
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        mDrawerToggle = new ActionBarDrawerToggleCompat(this, mDrawerLayout, mToolbar);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mTitleFooter = (TextView) this.findViewById(R.id.titleFooter);
        mIconFooter = (ImageView) this.findViewById(R.id.iconFooter);

        mFooterDrawer = (RelativeLayout) this.findViewById(R.id.footerDrawer);
        mFooterDrawer.setOnClickListener(onClickFooterDrawer);

        mRelativeDrawer = (FrameLayout) this.findViewById(R.id.relativeDrawer);

        //alvin temp comments
        this.setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setTitle("");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Resources.Theme theme = this.getTheme();
                TypedArray typedArray = theme.obtainStyledAttributes(new int[]{android.R.attr.colorPrimary});
                mDrawerLayout.setStatusBarBackground(typedArray.getResourceId(0, 0));
            } catch (Exception e) {
                e.getMessage();
            }

            this.setElevationToolBar(15);
        }

        if (mList != null) {
            mountListNavigation(savedInstanceState);
        }

        if (savedInstanceState == null) {
            mNavigationListener.onItemClickNavigation(mCurrentPosition, R.id.container);
        }

        setCheckedItemNavigation(mCurrentPosition, true);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub		
		super.onSaveInstanceState(outState);		
		outState.putInt(CURRENT_POSITION, mCurrentPosition);
		
		
		//FORM WIZARD
		    outState.putBundle("model", mWizardModel.save());
	        outState.putBoolean("dataChanged", mDataChanged);
	}
	
	//Adding Event To the Menu items at the ToolBar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menu_add) {
			Toast.makeText(getApplicationContext(), "HOME BUTTON", Toast.LENGTH_SHORT)
					.show();
		}

		if (mDrawerToggle != null) {
			if (mDrawerToggle.onOptionsItemSelected(item)) {
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.findItem(R.id.menu_add).setVisible(true);
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mRelativeDrawer);
    	mNavigationListener.onPrepareOptionsMenuNavigation(menu, mCurrentPosition, drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
	 }

	private class ActionBarDrawerToggleCompat extends ActionBarDrawerToggle {

        public ActionBarDrawerToggleCompat(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar){
            super(
                    activity,
                    drawerLayout, toolbar,
                    R.string.drawer_open,
                    R.string.drawer_close);
        }
//ALVIN
		@Override
		public void onDrawerClosed(View view) {			
			supportInvalidateOptionsMenu();
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			supportInvalidateOptionsMenu();
		}		
	}
		  
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
	}
	
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            int mPosition = (position - 1);

            if (position != 0) {
                mNavigationListener.onItemClickNavigation(mPosition, R.id.container);
                setCurrentPosition(mPosition);
                setCheckedItemNavigation(mPosition, true);
            }

	    	mDrawerLayout.closeDrawer(mRelativeDrawer);
        }
    }

    private OnClickListener onClickUserPhoto = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
            mNavigationListener.onClickUserPhotoNavigation(v);
			mDrawerLayout.closeDrawer(mRelativeDrawer);
		}
	};

    private OnClickListener onClickFooterDrawer = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mNavigationListener.onClickFooterItemNavigation(v);
            mDrawerLayout.closeDrawer(mRelativeDrawer);
        }
    };

    private void mountListNavigation(Bundle savedInstanceState){
        createUserDefaultHeader();
        onUserInformation();
        onInt(savedInstanceState);
        setAdapterNavigation();
    }

    private void setAdapterNavigation(){

        if (mNavigationListener == null){
            throw new RuntimeException("You must start the NavigationListener in onInit() method of its main activity. Example: this.setNavigationListener(this);");
        }

        mNavigationAdapter = new NavigationLiveoAdapter(this, NavigationLiveoList.getNavigationAdapter(mListNameItem, mListIcon,
                mListHeader, mSparseCounter, mColorSelected, mRemoveSelector), mNewSelector, mColorDefault, mRemoveAlpha);

        mList.setAdapter(mNavigationAdapter);
    }

    /**
     * Create user default header
     */
    private void createUserDefaultHeader() {
        mHeader = getLayoutInflater().inflate(R.layout.navigation_list_header, mList, false);

        mUserName = (TextView) mHeader.findViewById(R.id.userName);
        mUserEmail = (TextView) mHeader.findViewById(R.id.userEmail);

        mUserPhoto = (ImageView) mHeader.findViewById(R.id.userPhoto);
        mUserPhoto.setOnClickListener(onClickUserPhoto);

        mUserBackground = (ImageView) mHeader.findViewById(R.id.userBackground);
        mList.addHeaderView(mHeader);
    }

    /**
     * Set adapter attributes
     * @param listNameItem list name item.
     * @param listIcon list icon item.
     * @param listItensHeader list header name item.
     * @param sparceItensCount sparce count item.
     */
    public void setNavigationAdapter(List<String> listNameItem, List<Integer> listIcon, List<Integer> listItensHeader, SparseIntArray sparceItensCount){
        this.mListNameItem = listNameItem;
        this.mListIcon = listIcon;
        this.mListHeader = listItensHeader;
        this.mSparseCounter = sparceItensCount;
    }

    /**
     * Set adapter attributes
     * @param listNameItem list name item.
     * @param listIcon list icon item.
     */
    public void setNavigationAdapter(List<String> listNameItem, List<Integer> listIcon){
        this.mListNameItem = listNameItem;
        this.mListIcon = listIcon;
    }

    /**
     * Starting listener navigation
     * @param navigationListener listener.
     */
    public void setNavigationListener(NavigationLiveoListener navigationListener){
        this.mNavigationListener = navigationListener;
    };

    /**
     * First item of the position selected from the list
     * @param position ...
     */
    public void setDefaultStartPositionNavigation(int position){
        this.mCurrentPosition = position;
    }

    /**
     * Position in the last clicked item list
     * @param position ...
     */
    private void setCurrentPosition(int position){
        this.mCurrentPosition = position;
    }

    /**
     * get position in the last clicked item list
     */
    public int getCurrentPosition(){
        return this.mCurrentPosition;
    }

    /*{  }*/

    /**
     * Select item clicked
     * @param position item position.
     * @param checked true to check.
     */
    public void setCheckedItemNavigation(int position, boolean checked){
        this.mNavigationAdapter.resetarCheck();
        this.mNavigationAdapter.setChecked(position, checked);
    }

    /**
     * Information footer list item
     * @param title item footer name.
     * @param icon item footer icon.
     */
    public void setFooterInformationDrawer(String title, int icon){

        if (title == null){
            throw new RuntimeException("The title can not be null or empty");
        }

        if (title.trim().equals("")){
            throw new RuntimeException("The title can not be null or empty");
        }

        mTitleFooter.setText(title);

        if (icon == 0){
            mIconFooter.setVisibility(View.GONE);
        }else{
            mIconFooter.setImageResource(icon);
        }
    };

    /**
     * Information footer list item
     * @param title item footer name.
     * @param icon item footer icon.
     */
    public void setFooterInformationDrawer(int title, int icon){

        if (title == 0){
            throw new RuntimeException("The title can not be null or empty");
        }

        mTitleFooter.setText(getString(title));

        if (icon == 0){
            mIconFooter.setVisibility(View.GONE);
        }else{
            mIconFooter.setImageResource(icon);
        }
    };

    /**
     * If not want to use the footer item just put false
     * @param visible true or false.
     */
    public void setFooterNavigationVisible(boolean visible){
        this.mFooterDrawer.setVisibility((visible) ? View.VISIBLE : View.GONE);
    }

    /**
     * Item color selected in the list - name and icon (use before the setNavigationAdapter)
     * @param colorId color id.
     */
    public void setColorSelectedItemNavigation(int colorId){
        this.mColorSelected = colorId;
    }

    /**
     * Footer icon color
     * @param colorId color id.
     */
    public void setFooterIconColorNavigation(int colorId){
        this.mIconFooter.setColorFilter(getResources().getColor(colorId));
    }

    /**
     * Item color default in the list - name and icon (use before the setNavigationAdapter)
     * @param colorId color id.
     */
    public void setColorDefaultItemNavigation(int colorId){
        this.mColorDefault = colorId;
    }

    /**
     * New selector navigation
     * @param drawable drawable xml - selector.
     */
    public void setNewSelectorNavigation(int drawable){

        if (mRemoveSelector){
            throw new RuntimeException("The option to remove the select is active. Please remove the removeSelectorNavigation method so you can use the setNewSelectorNavigation");
        }

        this.mNewSelector = drawable;
    }

    /**
     * Remove selector navigation
     */
    public void removeSelectorNavigation(){
        this.mRemoveSelector = true;
    }

    /**
     * New counter value
     * @param position item position.
     * @param value new counter value.
     */
    public void setNewCounterValue(int position, int value){
        this.mNavigationAdapter.setNewCounterValue(position, value);
    }

    /**
     * Increasing counter value
     * @param position item position.
     * @param value new counter value (old value + new value).
     */
    public void setIncreasingCounterValue(int position, int value){
        this.mNavigationAdapter.setIncreasingCounterValue(position, value);
    }

    /**
     * Decrease counter value
     * @param position item position.
     * @param value new counter value (old value - new value).
     */
    public void setDecreaseCountervalue(int position, int value){
        this.mNavigationAdapter.setDecreaseCountervalue(position, value);
    }

    /**
     * Remove alpha item navigation (use before the setNavigationAdapter)
     */
    public void removeAlphaItemNavigation(){
        this.mRemoveAlpha = !mRemoveAlpha;
    }

    /**
     * public void setElevation (float elevation)
     * Added in API level 21
     * Default value is 15
     * @param elevation Sets the base elevation of this view, in pixels.
     */
    public void setElevationToolBar(float elevation){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
           // this.getToolbar().setElevation(elevation); ALVIN TEMP COMMENT
        }
    }

    /**
     * Remove default Header
     */
    public void showDefauldHeader() {
        if (mHeader == null){
            throw new RuntimeException("header was not created");
        }

        mList.addHeaderView(mHeader);
    }

    /**
     * Remove default Header
     */
    private void removeDefauldHeader() {
        if (mHeader == null){
            throw new RuntimeException("header was not created");
        }

        mList.removeHeaderView(mHeader);
    }

    /**
     * Add custom Header
     * @param v ...
     */
    public void addCustomHeader(View v) {
        if (v == null){
            throw new RuntimeException("header custom was not created");
        }

        removeDefauldHeader();
        mList.addHeaderView(v);
    }

    /**
     * Remove default Header
     * @param v ...
     */
    public void removeCustomdHeader(View v) {
        if (v == null){
            throw new RuntimeException("header custom was not created");
        }

        mList.removeHeaderView(v);
    }

    /**
     * get listview
     */
    public ListView getListView() {
        return this.mList;
    }

    /**
     * get toolbar
     */
    public Toolbar getToolbar() {
        return this.mToolbar;
    }

    /**
     * Open drawer
     */
    public void openDrawer() {
        mDrawerLayout.openDrawer(mRelativeDrawer);
    }

    /**
     * Close drawer
     */
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mRelativeDrawer);
    }

    @Override
    public void onBackPressed() {

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mRelativeDrawer);
        if (drawerOpen) {
            mDrawerLayout.closeDrawer(mRelativeDrawer);
        } else {
            super.onBackPressed();
        }
    }
}
