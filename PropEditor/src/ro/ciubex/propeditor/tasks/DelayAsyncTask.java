/**
 * This file is part of PropEditor application.
 *
 * Copyright (C) 2016 Claudiu Ciobotariu
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
package ro.ciubex.propeditor.tasks;

import android.os.AsyncTask;

import ro.ciubex.propeditor.models.Constants;

/**
 * An asynchronous task used to wait an amount of time.
 *
 * @author Claudiu Ciobotariu
 *
 */
public class DelayAsyncTask extends
        AsyncTask<Void, Void, DefaultAsyncTaskResult> {

    private Responder mListener;
    private long mTime;

    /**
     * Responder used on delay process.
     */
    public interface Responder {
        void startDelayPeriod();

        void endDelayPeriod(DefaultAsyncTaskResult result);
    }

    /**
     * Constructor of this asynchronous task.
     *
     * @param listener The task responder.
     * @param time     The time to sleep in milliseconds.
     */
    public DelayAsyncTask(Responder listener, long time) {
        this.mListener = listener;
        this.mTime = time;
    }

    @Override
    protected DefaultAsyncTaskResult doInBackground(Void... params) {
        DefaultAsyncTaskResult result = new DefaultAsyncTaskResult();
        result.resultId = Constants.OK;
        try {
            Thread.sleep(mTime);
        } catch (InterruptedException e) {
            result.resultId = Constants.ERROR;
            result.resultMessage = e.getMessage();
        }
        return result;
    }


    /**
     * Method invoked on the UI thread before the task is executed.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.startDelayPeriod();
    }

    /**
     * Method invoked on the UI thread after the background computation
     * finishes.
     */
    @Override
    protected void onPostExecute(DefaultAsyncTaskResult result) {
        super.onPostExecute(result);
        mListener.endDelayPeriod(result);
    }
}
