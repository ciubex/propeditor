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

import ro.ciubex.propeditor.properties.Entities;
import ro.ciubex.propeditor.properties.Entity;
import android.widget.Filter;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class PropertiesListFilter extends Filter {
	private PropertiesListAdapter adapter;

	public PropertiesListFilter(PropertiesListAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * Method used to filter the data according to the constraint.
	 * 
	 * @param constraint
	 *            The specified constraint.
	 * @return The results of the filtering operation.
	 */
	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		FilterResults results = new FilterResults();
		results.values = null;
		results.count = -1;
		Entities properties = adapter.getOriginalProperties();
		if (constraint.length() > 0 && !properties.isEmpty()) {
			String filter = constraint.toString().trim();
			if (filter.length() > 0) {
				filter = filter.toLowerCase(adapter.getApplication()
						.getDefaultLocale());
				Entities newProperties = new Entities();
				String key, value;
				for (Entity entry : properties.getProperties()) {
					key = entry.getKey();
					if (key != null) {
						key = key.toLowerCase(adapter.getApplication()
								.getDefaultLocale());
					} else {
						key = "";
					}
					value = entry.getContent();
					if (value != null) {
						value = value.toLowerCase(adapter.getApplication()
								.getDefaultLocale());
					} else {
						value = "";
					}
					if (key.indexOf(filter) > -1 || value.indexOf(filter) > -1) {
						newProperties.add(entry);
					}
				}
				results.count = newProperties.size();
				if (results.count > 0) {
					results.values = newProperties;
				}
			}
		} else if (!properties.isEmpty()) {
			results.count = properties.size();
			results.values = properties.clone();
		}
		return results;
	}

	/**
	 * Method used to invoke the UI thread to publish the filtering results in
	 * the user interface.
	 * 
	 * @param constraint
	 *            The specified constraint.
	 * @param results
	 *            The results of the filtering operation.
	 */
	@Override
	protected void publishResults(CharSequence constraint, FilterResults results) {
		if (results.count > -1) {
			adapter.setProperties((Entities) results.values);
			adapter.notifyDataSetChanged();
		} else {
			adapter.notifyDataSetInvalidated();
		}
	}

}
