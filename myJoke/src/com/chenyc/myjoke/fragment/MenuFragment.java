package com.chenyc.myjoke.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chenyc.myjoke.MainActivity;
import com.chenyc.myjoke.R;

public class MenuFragment extends ListFragment {

	private List<Menu> menus = new ArrayList<Menu>();

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.menu_list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] menuTitles = getResources()
				.getStringArray(R.array.menu_titles);
		TypedArray menuImgIds = getResources().obtainTypedArray(
				R.array.menu_imgs);
		for (int i = 0; i < menuTitles.length; i++) {
			Menu menu = new Menu();
			menu.title = menuTitles[i];
			menu.img = menuImgIds.getDrawable(i);
			menus.add(menu);
		}

		setListAdapter(new MenuAdapter(menus));
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		switchFragment(position);
	}

	// the meat of switching the above fragment
	private void switchFragment(int position) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof MainActivity) {
			MainActivity ra = (MainActivity) getActivity();
			ra.switchContent(position);
		}
	}

	private class Menu {
		public Drawable img;
		public String title;
	}

	private class MenuAdapter extends BaseAdapter {

		private List<Menu> menus;

		public MenuAdapter(List<Menu> menus) {
			this.menus = menus;
		}

		@Override
		public int getCount() {
			return menus.size();
		}

		@Override
		public Object getItem(int pos) {
			return menus.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int pos, View arg1, ViewGroup arg2) {
			View view = View.inflate(getActivity(), R.layout.menu_list_item,
					null);
			Menu menu = menus.get(pos);
			TextView menuText = (TextView) view.findViewById(R.id.menu_text);
			ImageView menuImg = (ImageView) view.findViewById(R.id.menu_img);
			menuText.setText(menu.title);
			menuImg.setImageDrawable(menu.img);
			return view;
		}

	}

}
