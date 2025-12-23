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

            // Buscamos si estamos en la ventana de tiempo de alguna toma (hora exacta + 1 hora)
            // Para la primera toma, permitimos un pequeño margen previo de 5 minutos
            boolean esPrimeraToma = (med.getUltimaTomaId() == null || med.getUltimaTomaId().isEmpty());
            String horaObjetivo = calcularTomaActual(med.getHorasToma(), esPrimeraToma);
            
            // Verificamos si hay tomas olvidadas
            String tomaOlvidada = verificarTomaOlvidada(med.getHorasToma(), med.getUltimaTomaId(), fechaHoy);
            
            // Verificamos si hay alguna toma registrada hoy
            String horaUltimaTomada = obtenerHoraUltimaTomada(med.getUltimaTomaId(), fechaHoy);
            
            if (horaObjetivo == null) {
                // No estamos en ninguna ventana de tiempo de toma
                // Verificamos si hay una toma registrada hoy para mostrar "TOMADA"
                if (horaUltimaTomada != null) {
                    btnTomar.setText("TOMADA (" + horaUltimaTomada + ")");
                    btnTomar.setEnabled(false);
                    btnTomar.setAlpha(0.5f);
                    btnTomar.setTextColor(Color.BLACK);
                    btnTomar.setBackgroundColor(Color.GRAY);
                    btnTomar.setOnClickListener(null);
                } else {
                    btnTomar.setText("NO ES LA HORA");
                    btnTomar.setEnabled(false);
                    btnTomar.setAlpha(0.5f);
                    btnTomar.setTextColor(Color.BLACK);
                    btnTomar.setBackgroundColor(Color.GRAY);
                    btnTomar.setOnClickListener(null);
                }
            } else {
                // Estamos en la ventana de tiempo de una toma
                String tomaIdHoy = fechaHoy + "_" + horaObjetivo;
                
                // Comprobamos si esta toma específica ya fue registrada
                boolean yaTomada = med.getUltimaTomaId() != null && med.getUltimaTomaId().equals(tomaIdHoy);

                if (yaTomada) {
                    btnTomar.setText("TOMADA (" + horaObjetivo + ")");
                    btnTomar.setEnabled(false);
                    btnTomar.setAlpha(0.5f);
                    btnTomar.setTextColor(Color.BLACK);
                    btnTomar.setBackgroundColor(Color.GRAY);
                    btnTomar.setOnClickListener(null);
                } else {
                    btnTomar.setText("TOMAR (" + horaObjetivo + ")");
                    btnTomar.setEnabled(true);
                    btnTomar.setAlpha(1.0f);
                    btnTomar.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.purple_500));
                    
                    // Clic en botón Tomar
                    btnTomar.setOnClickListener(v -> {
                        if (listener != null) {
                            // Si hay una toma olvidada, mostrar advertencia
                            if (tomaOlvidada != null) {
                                new android.app.AlertDialog.Builder(v.getContext())
                                    .setTitle("Toma olvidada")
                                    .setMessage("No marcaste como tomada la dosis de las " + tomaOlvidada + ". ¿Quieres marcar la toma actual de las " + horaObjetivo + "?")
                                    .setPositiveButton("Sí, marcar actual", (dialog, which) -> {
                                        listener.onTomarClick(med, tomaIdHoy);
                                    })
                                    .setNegativeButton("Cancelar", null)
                                    .show();
                            } else {
                                listener.onTomarClick(med, tomaIdHoy);
                            }
                        }
                    });
                }
            }

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
         * Determina si estamos en la ventana de tiempo de alguna toma programada.
         * Retorna la hora de la toma si estamos desde la hora exacta hasta +60 minutos después.
         * Para la primera toma, permite un margen de -5 minutos para facilitar el registro inicial.
         * Retorna null si no estamos en ninguna ventana de tiempo.
         */
        private String calcularTomaActual(List<String> horas, boolean esPrimeraToma) {
            if (horas == null || horas.isEmpty()) return null;

            // Obtenemos hora actual en formato HH:mm
            String horaActual = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
            
            // Convertimos la hora actual a minutos desde medianoche
            int minutosActuales = convertirHoraAMinutos(horaActual);

            // Ventana de tiempo: desde la hora exacta hasta +60 minutos después
            // Para la primera toma, permitimos -5 minutos de margen
            final int MARGEN_MINUTOS = 60;
            final int MARGEN_PREVIO_PRIMERA_TOMA = 5;

            // Buscamos si estamos dentro de la ventana de alguna toma
            for (String h : horas) {
                int minutosHora = convertirHoraAMinutos(h);
                int diferencia = minutosActuales - minutosHora;

                // Si es la primera toma, permitimos desde -5 hasta +60 minutos
                if (esPrimeraToma) {
                    if (diferencia >= -MARGEN_PREVIO_PRIMERA_TOMA && diferencia <= MARGEN_MINUTOS) {
                        return h;
                    }
                } else {
                    // Tomas posteriores: desde la hora exacta hasta +60 minutos
                    if (diferencia >= 0 && diferencia <= MARGEN_MINUTOS) {
                        return h;
                    }
                }
            }

            // No estamos en ninguna ventana de tiempo
            return null;
        }

        /**
         * Obtiene la hora de la última toma registrada si es del día de hoy.
         * Retorna la hora en formato "HH:mm" o null si no hay toma de hoy.
         */
        private String obtenerHoraUltimaTomada(String ultimaTomaId, String fechaHoy) {
            if (ultimaTomaId != null && ultimaTomaId.startsWith(fechaHoy)) {
                String[] partes = ultimaTomaId.split("_");
                if (partes.length > 1) {
                    return partes[1];
                }
            }
            return null;
        }

        /**
         * Verifica si hay alguna toma que el usuario olvidó marcar.
         * Retorna la hora de la toma olvidada, o null si no hay ninguna.
         */
        private String verificarTomaOlvidada(List<String> horas, String ultimaTomaId, String fechaHoy) {
            if (horas == null || horas.isEmpty()) return null;

            // Obtenemos hora actual en minutos
            String horaActual = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
            int minutosActuales = convertirHoraAMinutos(horaActual);

            // Extraemos la última hora tomada (si existe y es de hoy)
            String ultimaHoraTomada = null;
            if (ultimaTomaId != null && ultimaTomaId.startsWith(fechaHoy)) {
                String[] partes = ultimaTomaId.split("_");
                if (partes.length > 1) {
                    ultimaHoraTomada = partes[1];
                }
            }

            // Buscamos tomas que ya pasaron (más de 60 minutos) y no fueron marcadas
            for (String h : horas) {
                int minutosHora = convertirHoraAMinutos(h);
                int diferencia = minutosActuales - minutosHora;

                // Si la hora ya pasó hace más de 60 minutos
                if (diferencia > 60) {
                    // Y no es la última que se marcó como tomada
                    if (ultimaHoraTomada == null || !h.equals(ultimaHoraTomada)) {
                        return h; // Esta toma se olvidó
                    }
                }
            }

            return null; // No hay tomas olvidadas
        }

        /**
         * Convierte una hora en formato "HH:mm" a minutos desde medianoche.
         */
        private int convertirHoraAMinutos(String hora) {
            try {
                String[] partes = hora.split(":");
                int horas = Integer.parseInt(partes[0]);
                int minutos = Integer.parseInt(partes[1]);
                return horas * 60 + minutos;
            } catch (Exception e) {
                return 0;
            }
        }
    }
}