package com.example.pillminder.adapter; // O el paquete que hayas elegido

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillminder.R;
import com.example.pillminder.model.Medicamento;

/**
 * Adaptador para mostrar la lista de Medicamentos.
 * Usa ListAdapter y DiffUtil para manejar las actualizaciones de la lista eficientemente.
 */
public class MedicamentoAdapter extends ListAdapter<Medicamento, MedicamentoAdapter.MedicamentoViewHolder> {

    // Interfaz para manejar clics en el botón "Tomada"
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        // Se pueden añadir más acciones si fueran necesarias (ej. onEditPill)
        void onTakePillClick(Medicamento medicamento);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    // Constructor que define cómo se comparan los objetos Medicamento para DiffUtil
    public MedicamentoAdapter() {
        super(DIFF_CALLBACK);
    }

    // ----------------------------------------------------
    // 1. DiffUtil: Optimiza las actualizaciones de la lista
    // ----------------------------------------------------
    private static final DiffUtil.ItemCallback<Medicamento> DIFF_CALLBACK = new DiffUtil.ItemCallback<Medicamento>() {
        @Override
        public boolean areItemsTheSame(@NonNull Medicamento oldItem, @NonNull Medicamento newItem) {
            // Firestore usa el documentId para identificar el elemento
            return oldItem.getDocumentId().equals(newItem.getDocumentId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Medicamento oldItem, @NonNull Medicamento newItem) {
            // Compara si el contenido del objeto ha cambiado (ej. dosis, stock)
            return oldItem.getNombre().equals(newItem.getNombre()) &&
                    oldItem.getDosisCantidad() == newItem.getDosisCantidad() &&
                    oldItem.getStockTotal() == newItem.getStockTotal() &&
                    oldItem.getHoraToma().equals(newItem.getHoraToma());
        }
    };

    // ----------------------------------------------------
    // 2. Creación del ViewHolder (Infla el XML)
    // ----------------------------------------------------
    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(itemView);
    }

    // ----------------------------------------------------
    // 3. Conexión de Datos (Bind)
    // ----------------------------------------------------
    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento currentPill = getItem(position);

        // Muestra el nombre y la dosis
        holder.tvName.setText(currentPill.getNombre());
        holder.tvDose.setText(currentPill.getDosisCantidad() + " ud.");

        // Muestra los horarios
        holder.tvSchedule.setText("Horarios: " + currentPill.getHoraToma());

        // Muestra el stock
        holder.tvStock.setText("Stock: " + currentPill.getStockTotal() + " unidades");
    }

    // ----------------------------------------------------
    // 4. ViewHolder: Mantiene las referencias a las vistas
    // ----------------------------------------------------
    class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvDose;
        private final TextView tvSchedule;
        private final TextView tvStock;
        private final Button btnTakePill;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_pill_name);
            tvDose = itemView.findViewById(R.id.tv_pill_dose);
            tvSchedule = itemView.findViewById(R.id.tv_pill_schedule);
            tvStock = itemView.findViewById(R.id.tv_pill_stock);
            btnTakePill = itemView.findViewById(R.id.btn_take_pill);

            // Listener para el botón "Tomada"
            btnTakePill.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    // Notifica a la MainActivity que se hizo clic en "Tomada" para este medicamento
                    listener.onTakePillClick(getItem(position));
                }
            });

            // Listener para toda la fila (útil para la edición o más detalles)
            itemView.setOnClickListener(v -> {
                // Aquí podrías añadir lógica para abrir el formulario de edición
            });
        }
    }
}