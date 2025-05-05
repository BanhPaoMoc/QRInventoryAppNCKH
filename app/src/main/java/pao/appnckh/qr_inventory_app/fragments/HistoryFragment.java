package pao.appnckh.qr_inventory_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.adapters.TransactionHistoryAdapter;
import pao.appnckh.qr_inventory_app.models.TransactionHistory;

public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView txtEmptyHistory;
    private TransactionHistoryAdapter adapter;
    private List<TransactionHistory> transactionList;
    private DatabaseReference databaseRef;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        txtEmptyHistory = view.findViewById(R.id.txtEmptyHistory);

        // Initialize
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        transactionList = new ArrayList<>();
        adapter = new TransactionHistoryAdapter(transactionList);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        // Setup Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .child("TransactionHistory");

        loadTransactionHistory();

        return view;
    }

    private void loadTransactionHistory() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TransactionHistory transaction = dataSnapshot.getValue(TransactionHistory.class);
                    if (transaction != null) {
                        transactionList.add(transaction);
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(transactionList, (t1, t2) ->
                        Long.compare(t2.getTimestamp(), t1.getTimestamp()));

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                txtEmptyHistory.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                txtEmptyHistory.setText("Lỗi tải dữ liệu: " + error.getMessage());
            }
        });
    }

    private void updateUI() {
        if (transactionList.isEmpty()) {
            txtEmptyHistory.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmptyHistory.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }
}