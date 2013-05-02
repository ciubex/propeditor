/**
 * This file is part of PropEditor application.
 * 
 * Copyright (C) 2013 Claudiu Ciobotariu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ro.ciubex.propeditor.list;

import ro.ciubex.propeditor.PropEditorApplication;
import ro.ciubex.propeditor.R;
import ro.ciubex.propeditor.properties.Entities;
import ro.ciubex.propeditor.properties.Entity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

/**
 * This is the list adapter used to populate the listView.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class PropertiesListAdapter extends BaseAdapter {
	private PropEditorApplication application;
	private Entities originalProperties;
	private Entities properties;
	private LayoutInflater mInflater;
	private PropertiesListFilter filter;

	public PropertiesListAdapter(Context context,
			PropEditorApplication application, Entities properties) {
		this.application = application;
		this.originalProperties = properties;
		this.properties = (Entities) properties.clone();
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Used to obtain properties size
	 * 
	 * @return Properties size
	 */
	@Override
	public int getCount() {
		return properties != null ? properties.size() : 0;
	}

	/**
	 * Retrieve the string value from properties list from specified position.
	 * If the properties is empty the null is returned
	 * 
	 * @param position
	 *            The position in list of properties for retrieve the value
	 * @return The property value from specified position
	 */
	@Override
	public Entity getItem(int position) {
		return properties.getProperty(position);
	}

	/**
	 * Get the row id associated with the specified position in the list. In
	 * this case the position is also the id.
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Set the properties to the adapter
	 * 
	 * @param properties
	 *            The properties to set
	 */
	public void setProperties(Entities properties) {
		this.properties = properties;
	}

	/**
	 * Get the properties of this adapter
	 * 
	 * @return The properties
	 */
	public Entities getProperties() {
		return properties;
	}

	/**
	 * Get original properties of this adapter
	 * 
	 * @return Original properties of this adapter
	 */
	public Entities getOriginalProperties() {
		return originalProperties;
	}

	/**
	 * Retrieve the application
	 * 
	 * @return The application
	 */
	public PropEditorApplication getApplication() {
		return application;
	}

	/**
	 * Get a View that displays the data at the specified position in the data
	 * set.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param view
	 *            The old view to reuse, if possible.
	 * @param parent
	 *            The parent that this view will eventually be attached to.
	 * @return A View corresponding to the data at the specified position.
	 */
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		PropertyViewHolder viewHolder = null;
		if (view != null) {
			viewHolder = (PropertyViewHolder) view.getTag();
		} else {
			view = mInflater.inflate(R.layout.list_item_layout, null);
			viewHolder = new PropertyViewHolder();
			viewHolder.firstItemText = (TextView) view
					.findViewById(R.id.firstItemText);
			viewHolder.secondItemText = (TextView) view
					.findViewById(R.id.secondItemText);
			view.setTag(viewHolder);
		}
		if (viewHolder != null) {
			Entity property = getItem(position);
			if (property != null) {
				viewHolder.firstItemText.setText(property.getKey());
				viewHolder.secondItemText.setText(property.getContent());
			}
		}
		return view;
	}

	/**
	 * Used to obtain the adapter filter
	 * 
	 * @return Adapter customized filter
	 */
	public Filter getFilter() {
		if (filter == null)
			filter = new PropertiesListFilter(this);
		return filter;
	}

	/**
	 * View holder for properties list
	 * 
	 */
	static class PropertyViewHolder {
		TextView firstItemText;
		TextView secondItemText;
	}

}
