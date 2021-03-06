package cosmin.licenta.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

import cosmin.licenta.Adapters.CurrencyAdapter;
import cosmin.licenta.Common.Exchange;
import cosmin.licenta.Common.Helper;
import cosmin.licenta.Common.HttpRequest;
import cosmin.licenta.Common.MyConstants;
import cosmin.licenta.R;


public class CurrencyFragment extends Fragment {

    public Context mContext;

    public CurrencyAdapter mAdapter = null;
    public ArrayList<Exchange> mExchangeRates;
    private ListView mListView;

    public CurrencyFragment() {
    }

    public static CurrencyFragment newInstance() {
        return new CurrencyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_currency, container, false);
        mContext = getContext();
        mListView = rootView.findViewById(R.id.currency_list);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> params = new HashMap<>();
                params.put(MyConstants.paramsType, MyConstants.paramsCurrency);
                params.put(MyConstants.paramsCurrency, mAdapter.getItem(position).getRate());
                Helper.getInstance().showDialog(getContext(), params);
            }
        });

        mExchangeRates = new ArrayList<>();

        //todo - create a graphic from more data

        new GetRatesTask().execute();

        return rootView;
    }

    public String getSpecificCurrency(String currencyType,double value){
        for(int i=0;i<mAdapter.getCount();i++){
            if(currencyType.equals(mAdapter.getItem(i).getType())){
                double rate = Double.valueOf(mAdapter.getItem(i).getRate());
                double result = value/rate;
                return String.valueOf(result);
            }
        }
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class GetRatesTask extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mExchangeRates.clear();
            pDialog = new ProgressDialog(mContext);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            Document doc = new HttpRequest().connect();

            NodeList nList = doc.getElementsByTagName("Rate");

            for (int i = 0; i < nList.getLength(); i++) {

                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    Exchange rate = new Exchange();
                    rate.setType(element.getAttribute("currency"));
                    rate.setRate(element.getTextContent());

                    mExchangeRates.add(rate);
                }
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(String response) {
            mAdapter = new CurrencyAdapter(mContext, R.layout.list_item_currency, mExchangeRates);
            mListView.setAdapter(mAdapter);
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

}
