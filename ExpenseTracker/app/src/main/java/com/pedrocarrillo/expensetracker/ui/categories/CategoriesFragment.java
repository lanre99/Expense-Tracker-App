package com.pedrocarrillo.expensetracker.ui.categories;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.pedrocarrillo.expensetracker.R;
import com.pedrocarrillo.expensetracker.adapters.CategoriesAdapter;
import com.pedrocarrillo.expensetracker.custom.DefaultRecyclerViewItemDecorator;
import com.pedrocarrillo.expensetracker.entities.Category;
import com.pedrocarrillo.expensetracker.interfaces.IExpensesType;
import com.pedrocarrillo.expensetracker.ui.MainActivity;
import com.pedrocarrillo.expensetracker.ui.MainFragment;
import com.pedrocarrillo.expensetracker.utils.DialogManager;
import com.pedrocarrillo.expensetracker.utils.RealmManager;
import com.pedrocarrillo.expensetracker.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pcarrillo on 17/09/2015.
 */
public class CategoriesFragment extends MainFragment implements TabLayout.OnTabSelectedListener, CategoriesAdapter.CategoriesAdapterOnCLickHandler {

    private @IExpensesType int mCurrentMode = IExpensesType.MODE_EXPENSES;

//    private List<String> tabList;
    private RecyclerView rvCategories;
    private TextView tvEmpty;

    private List<Category> mCategoryList;
    private CategoriesAdapter mCategoriesAdapter;

    public static CategoriesFragment newInstance() {
        return new CategoriesFragment();
    }

    public CategoriesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategoryList = new ArrayList<>();
        mCategoriesAdapter = new CategoriesAdapter(mCategoryList, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);
        rvCategories = (RecyclerView)rootView.findViewById(R.id.rv_categories);
        tvEmpty = (TextView)rootView.findViewById(R.id.tv_empty);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        tabList = Arrays.asList(getString(R.string.expenses), getString(R.string.income));
//        mMainActivityListener.setTabs(tabList, this);
        mMainActivityListener.setMode(MainActivity.NAVIGATION_MODE_STANDARD);
        reloadData();
        mMainActivityListener.setFAB(R.drawable.ic_add_white_48dp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCategoryDialog(null);
            }
        });
        mMainActivityListener.setTitle(getString(R.string.categories));

        rvCategories.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCategories.setAdapter(mCategoriesAdapter);
        rvCategories.setHasFixedSize(true);
        rvCategories.addItemDecoration(new DefaultRecyclerViewItemDecorator(getResources().getDimension(R.dimen.dimen_10dp)));

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final Category category = (Category) viewHolder.itemView.getTag();
                DialogManager.getInstance().createCustomAcceptDialog(getActivity(), getString(R.string.delete), getString(R.string.confirm_delete).concat(category.getName()), getString(R.string.confirm), getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            RealmManager.getInstance().delete(category);
                        }
                        reloadData();
                    }
                });
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvCategories);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getTag()!=null) {
            if (tab.getTag().toString().equalsIgnoreCase(getString(R.string.expenses))) {
                mCurrentMode = IExpensesType.MODE_EXPENSES;
            } else if (tab.getTag().toString().equalsIgnoreCase(getString(R.string.income))) {
                mCurrentMode = IExpensesType.MODE_INCOME;
            }
        }
        reloadData();
    }

    private void reloadData() {
        mCategoryList = Category.getCategoriesForType(mCurrentMode);
        if (mCategoryList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
//        if (mCategoriesAdapter == null) {
//            mCategoriesAdapter = new CategoriesAdapter(mCategoryList, this);
//        } else {
            mCategoriesAdapter.updateCategories(mCategoryList);
//        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(CategoriesAdapter.ViewHolder vh) {
        createCategoryDialog(vh);
    }

    private void createCategoryDialog(final CategoriesAdapter.ViewHolder vh) {
        AlertDialog alertDialog = DialogManager.getInstance().createEditTextDialog(getActivity(), getString(R.string.create_category), getString(R.string.save), getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    EditText etTest = (EditText) ((AlertDialog) dialog).findViewById(R.id.et_main);
                    if (!Util.isEmptyField(etTest)) {
                        Category category = new Category(etTest.getText().toString(), mCurrentMode);
                        if (vh != null) {
                            Category categoryToUpdate = (Category)vh.itemView.getTag();
                            category.setId(categoryToUpdate.getId());
                            RealmManager.getInstance().update(category);
                        } else {
                            RealmManager.getInstance().save(category, Category.class);
                        }
                        reloadData();
                    } else {
                        DialogManager.getInstance().showShortToast(getString(R.string.error_name));
                    }
                }
            }
        });
        if (vh != null) {
            EditText etCategoryName = (EditText) alertDialog.findViewById(R.id.et_main);
            Category category = (Category) vh.itemView.getTag();
            etCategoryName.setText(category.getName());
        }
    }

    // Action mode for categories.
    private ActionMode mActionMode;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.expenses_context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    @Override
    public void onLongClick(CategoriesAdapter.ViewHolder vh) {
        if (mActionMode == null) {
            mActionMode = mMainActivityListener.setActionMode(mActionModeCallback);
        }
//        toggleSelection(position);
    }
}