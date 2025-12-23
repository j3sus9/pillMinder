package com.example.pillminder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillminder.R;
import com.example.pillminder.model.Medicamento;

public class MedicamentoAdapter extends ListAdapter<Medicamento, MedicamentoAdapter.MedicamentoViewHolder> {

    private OnMedicamentoClickListener listener;

    // Interface para comunicar clics a la MainActivity
    public interface OnMedicamentoClickListener {
        void onTomarClick(Medicamento medicamento);
        void onBorrarClick(Medicamento medicamento);
        void onEditarClick(Medicamento medicamento);
    }

    public void setOnMedicamentoClickListener(OnMedicamentoClickListener listener) {
        this.listener = listener;
    }

    public MedicamentoAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Medicamento> DIFF_CALLBACK = new DiffUtil.ItemCallback<Medicamento>() {
        @Override
        public boolean areItemsTheSame(@NonNull Medicamento oldItem, @NonNull Medicamento newItem) {
            return oldItem.getDocumentId().equals(newItem.getDocumentId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Medicamento oldItem, @NonNull Medicamento newItem) {
            return oldItem.getNombre().equals(newItem.getNombre()) &&
                    oldItem.getStockTotal() == newItem.getStockTotal() &&
                    oldItem.getDosis() == newItem.getDosis();
        }
    };

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento med = getItem(position);
        holder.bind(med, listener);
    }

    class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDosis, tvStock, tvHora;
        Button btnTomar;
        ImageButton btnMenuOptions; // El de los tres puntitos

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_medicamento);
            tvDosis = itemView.findViewById(R.id.tv_dosis);
            tvStock = itemView.findViewById(R.id.tv_stock_disponible);
            tvHora = itemView.findViewById(R.id.tv_hora_toma);
            btnTomar = itemView.findViewById(R.id.btn_tomar);
            btnMenuOptions = itemView.findViewById(R.id.btn_menu_opciones);
        }

        public void bind(Medicamento med, OnMedicamentoClickListener listener) {
            tvNombre.setText(med.getNombre());
            tvDosis.setText("Dosis: " + med.getDosis());
            tvStock.setText("Stock: " + med.getStockTotal());
            tvHora.setText("Hora: " + med.getHoraToma());

            // 1. Clic en botón Tomar
            btnTomar.setOnClickListener(v -> {
                if (listener != null) listener.onTomarClick(med);
            });

            // 2. Clic en los tres puntitos (Menú Popup)
            btnMenuOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), btnMenuOptions);
                popup.inflate(R.menu.item_medicamento_menu); // El menú que creamos antes

                popup.setOnMenuItemClickListener(item -> {
                    if (listener == null) return false;

                    int id = item.getItemId();
                    if (id == R.id.action_delete) {
                        listener.onBorrarClick(med);
                        return true;
                    } else if (id == R.id.action_edit) {
                        listener.onEditarClick(med);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}