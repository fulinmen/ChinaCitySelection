package knight.chinacityselection.views;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import knight.chinacityselection.ClientApplication;
import knight.chinacityselection.R;
import knight.chinacityselection.adapter.GridViewAdapter;
import knight.chinacityselection.db.CityDB;


public class GridPop extends PopupWindow {
    private Context context;
    private LayoutInflater layoutInflater;
    public View allView;
    private int resId;
    private GridView allItemGrid;

    private GridView gv;
    private CityDB cityDB;
    private GridViewAdapter gridViewAdapter;

    //选择城市标志位
//    private boolean flagCitySelected = false;
    private FlagCitySelected flagCitySelected = FlagCitySelected.ZERO;

    private String mCurrentProvince;

    private ArrayList<String> mCurrentStringArray;
    private Button backBtn;

    private RelativeLayout mRLPopTitleAction;


    public interface onCitySelectedListener {
        public void onCitySelected(String city);
    }

    private onCitySelectedListener mOnCitySelectedListener;

    public void setOnCitySelectedListener(onCitySelectedListener l) {
        this.mOnCitySelectedListener = l;
    }

    public GridPop(Context context, int resourceId) {
        super(context);
        this.context = context;
        this.resId = resourceId;
        initAllPop();
        initData();

        initView();
    }

    /**
     * 初始化TitleAction
     */
    private void initTitleAction() {
        updateTitleAction();
    }

    /**
     * 更新TitleAction
     */
    private void updateTitleAction() {

        if (flagCitySelected.ordinal() == FlagCitySelected.ZERO.ordinal()) {
            mRLPopTitleAction.setVisibility(View.GONE);
        } else {
            mRLPopTitleAction.setVisibility(View.VISIBLE);
        }
    }

    private void initView() {

        mCurrentStringArray = (ArrayList<String>) cityDB.getAllProvince();

        gv = getAllItemGrid();
        gridViewAdapter = new GridViewAdapter(context, mCurrentStringArray);

        gv.setAdapter(gridViewAdapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (flagCitySelected) {
                    case ZERO:
                        mCurrentProvince = mCurrentStringArray.get(position);
                        flagCitySelected = FlagCitySelected.PROVINCE;
                        selectCity(mCurrentProvince);
                        break;
                    case PROVINCE:
                        String city = mCurrentStringArray.get(position);
                        flagCitySelected = FlagCitySelected.CITY;
                        selectCountry(mCurrentProvince, city);
                        break;
                    case CITY:
                        mOnCitySelectedListener.onCitySelected(mCurrentStringArray.get(position));
                        flagCitySelected = FlagCitySelected.ZERO;
                        GridPop.this.dismiss();
                        mCurrentStringArray.clear();
                        mCurrentStringArray.addAll(cityDB.getAllProvince());
                        gridViewAdapter.notifyDataSetChanged();
                        break;
                }
                updateTitleAction();
            }
        });


        mRLPopTitleAction = (RelativeLayout) allView.findViewById(R.id.layout_id_pop_grid);
        initTitleAction();

        //初始化 pop window action title
        backBtn = (Button) allView.findViewById(R.id.id_btn_pop_grid_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flagCitySelected = FlagCitySelected.ZERO;
                mCurrentStringArray.clear();
                mCurrentStringArray.addAll(cityDB.getAllProvince());
                gridViewAdapter.notifyDataSetChanged();

                updateTitleAction();
            }
        });
    }

    private void initData() {
        cityDB = ClientApplication.getInstance().getCityDB();
    }

    public void initAllPop() {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        allView = layoutInflater.inflate(resId, null);
        allView.setFocusable(true);
        allView.setFocusableInTouchMode(true);
        allItemGrid = (GridView) allView.findViewById(R.id.id_grid_view);
        allView.setFocusableInTouchMode(true);
        allView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (isShowing()) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        setContentView(allView);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
//        setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
    }

    /**
     * @return 返回该pop中GridView, 可以其他地方设置该GridView
     */
    public GridView getAllItemGrid() {
        return allItemGrid;
    }

    private void selectCity(String province) {
        mCurrentStringArray.clear();
        ArrayList<String> cityList = (ArrayList<String>) cityDB.getProvinceAllCity(province);
        mCurrentStringArray.addAll(cityList);
        gridViewAdapter.notifyDataSetChanged();
    }

    private void selectCountry(String province, String city) {
        mCurrentStringArray.clear();
        ArrayList<String> cityList = (ArrayList<String>) cityDB.getAllCountry(province, city);
        mCurrentStringArray.addAll(cityList);
        gridViewAdapter.notifyDataSetChanged();
    }

    /**
     * toggle gridPop windows
     *
     * @param v
     */
    public void toggle(final View v) {
        if (this.isShowing()) {
            this.dismiss();
        } else {
            this.showAsDropDown(v);
            this.setOnCitySelectedListener(new GridPop.onCitySelectedListener() {
                @Override
                public void onCitySelected(String city) {
                    ((Button) v).setText(city);
                }
            });
        }
    }

    private enum FlagCitySelected {
        ZERO, PROVINCE, CITY, COUNTRY;
    }
}
