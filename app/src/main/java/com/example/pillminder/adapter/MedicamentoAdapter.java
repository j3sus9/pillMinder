package com.example.pillminder.adapter;

import android.graphics.Color;
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
import com.example.pillminder.utils.FormatUtils;

import java.util.List;

public class MedicamentoAdapter extends ListAdapter<Medicamento, MedicamentoAdapter.MedicamentoViewHolder> {

    private OnMedicamentoClickListener listener;

    // Interface para comunicar clics a la MainActivity
    public interface OnMedicamentoClickListener {
        void onTomarClick(Medicamento medicamento, String tomaId);
        void onBorrarClick(Medicamento medicamento);
        void onEditarClick(Medicamento medicamento);
        void onReponerClick(Medicamento medicamento);
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
                    oldItem.getDosis() == newItem.getDosis() &&
                    oldItem.getHorasToma().equals(newItem.getHorasToma());
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
        ImageButton btnMenuOptions;

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

            String unidadDosis = FormatUtils.obtenerUnidadFormateada(med.getDosis(), med.getTipoDosis());
            String unidadStock = FormatUtils.obtenerUnidadFormateada(med.getStockTotal(), med.getTipoDosis());
            String quedan = med.getStockTotal() == 1 ? "Queda: " : "Quedan: ";

            tvDosis.setText(med.getDosis() + " " + unidadDosis);
            tvStock.setText(quedan + med.getStockTotal() + " " + unidadStock);

            // Formatear horas
            String horasTexto = "";
            if (med.getHorasToma() != null && !med.getHorasToma().isEmpty()) {
                horasTexto = String.join(", ", med.getHorasToma());
                tvHora.setText("Horas: " + horasTexto);
            }

            // Lógica de alerta de stock (Rojo si quedan 2 dosis o menos)
            if (med.getStockTotal() <= 2 * med.getDosis()) {
                tvStock.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvStock.setTextColor(itemView.getContext().getResources().getColor(android.R.color.tab_indicator_text));
            }

            // --- LÓGICA DE BLOQUEO "TOMADA" ---
            String fechaHoy = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(new java.util.Date());

            // Buscamos cuál es la toma que le toca ahora o la más reciente
            String horaObjetivo = calcularHoraMasCercana(med.getHorasToma());
            String tomaIdHoy = fechaHoy + "_" + horaObjetivo;

            // Comprobamos si el ID de la toma de hoy ya figura en el medicamento
            boolean yaTomada = med.getUltimaTomaId() != null && med.getUltimaTomaId().equals(tomaIdHoy);

            if (yaTomada) {
                btnTomar.setText("TOMADA (" + horaObjetivo + ")");
                btnTomar.setEnabled(false);
                btnTomar.setAlpha(0.5f);
                btnTomar.setTextColor(Color.BLACK);
                btnTomar.setBackgroundColor(Color.GRAY);
            } else {
                btnTomar.setText("TOMAR");
                btnTomar.setEnabled(true);
                btnTomar.setAlpha(1.0f);
                btnTomar.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.purple_500));
            }

            // Clic en botón Tomar
            btnTomar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTomarClick(med, tomaIdHoy);
                }
            });

            // Clic en los tres puntitos (Menú Popup)
            btnMenuOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), btnMenuOptions);
                popup.inflate(R.menu.item_medicamento_menu);

                popup.setOnMenuItemClickListener(item -> {
                    if (listener == null) return false;
                    int id = item.getItemId();
                    if (id == R.id.action_delete) {
                        listener.onBorrarClick(med);
                        return true;
                    } else if (id == R.id.action_edit) {
                        listener.onEditarClick(med);
                        return true;
                    } else if (id == R.id.action_reponer) {
                        listener.onReponerClick(med);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        /**
         * Función auxiliar para saber qué hora de la lista es la que toca mostrar en el botón
         */
        private String calcularHoraMasCercana(List<String> horas) {
            if (horas == null || horas.isEmpty()) return "";

            // Obtenemos hora actual en formato HH:mm
            String horaActual = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());

            // Por simplicidad, buscamos la primera hora de la lista que aún no ha pasado.
            // Si todas han pasado, devolvemos la última (la de la noche).
            for (String h : horas) {
                if (h.compareTo(horaActual) >= 0) {
                    return h;
                }
            }
            return horas.get(horas.size() - 1);
        }
    }
}