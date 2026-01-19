package com.example.usecase17;

import com.example.views.MainLayout;

import jakarta.annotation.security.PermitAll;

import java.util.Arrays;
import java.util.List;

import com.example.MissingAPI;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Use Case 17: Custom PC Builder - Complex State Management at Scale
 *
 * Demonstrates handling many signals (~70) with complex interdependencies: - 12
 * component selection signals - 40+ computed signals (price, power,
 * compatibility, performance) - 15+ validation/compatibility checks - 8 UI
 * state signals
 *
 * Key Patterns: - Scale: 70 signals vs typical 10-20 in other use cases -
 * Multi-level computed signals with dependencies - Complex cross-component
 * validation - Conditional options (filtered by previous selections) - Multiple
 * aggregation types (price, power, performance)
 */
@Route(value = "use-case-17", layout = MainLayout.class)
@PageTitle("Use Case 17: Custom PC Builder")
@Menu(order = 17, title = "UC 17: PC Builder (70 signals)")
@PermitAll
public class UseCase17View extends VerticalLayout {

    // ==================== Component Model Classes ====================

    public static class Component {
        protected String id;
        protected String name;
        protected double price;

        public Component(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class CPU extends Component {
        private String socket;
        private int tdp;
        private int score;

        public CPU(String id, String name, double price, String socket, int tdp,
                int score) {
            super(id, name, price);
            this.socket = socket;
            this.tdp = tdp;
            this.score = score;
        }

        public String getSocket() {
            return socket;
        }

        public int getTdp() {
            return tdp;
        }

        public int getScore() {
            return score;
        }
    }

    public static class Motherboard extends Component {
        private String socket;
        private String ramType;
        private int maxRamSpeed;
        private int maxRamCapacity;
        private String formFactor;
        private int m2Slots;
        private int sataSlots;

        public Motherboard(String id, String name, double price, String socket,
                String ramType, int maxRamSpeed, int maxRamCapacity,
                String formFactor, int m2Slots, int sataSlots) {
            super(id, name, price);
            this.socket = socket;
            this.ramType = ramType;
            this.maxRamSpeed = maxRamSpeed;
            this.maxRamCapacity = maxRamCapacity;
            this.formFactor = formFactor;
            this.m2Slots = m2Slots;
            this.sataSlots = sataSlots;
        }

        public String getSocket() {
            return socket;
        }

        public String getRamType() {
            return ramType;
        }

        public int getMaxRamSpeed() {
            return maxRamSpeed;
        }

        public int getMaxRamCapacity() {
            return maxRamCapacity;
        }

        public String getFormFactor() {
            return formFactor;
        }

        public int getM2Slots() {
            return m2Slots;
        }

        public int getSataSlots() {
            return sataSlots;
        }
    }

    public static class RAM extends Component {
        private String type;
        private int speed;
        private int capacity;

        public RAM(String id, String name, double price, String type, int speed,
                int capacity) {
            super(id, name, price);
            this.type = type;
            this.speed = speed;
            this.capacity = capacity;
        }

        public String getType() {
            return type;
        }

        public int getSpeed() {
            return speed;
        }

        public int getCapacity() {
            return capacity;
        }
    }

    public static class GPU extends Component {
        private int powerConsumption;
        private int lengthMm;
        private int score;

        public GPU(String id, String name, double price, int powerConsumption,
                int lengthMm, int score) {
            super(id, name, price);
            this.powerConsumption = powerConsumption;
            this.lengthMm = lengthMm;
            this.score = score;
        }

        public int getPowerConsumption() {
            return powerConsumption;
        }

        public int getLengthMm() {
            return lengthMm;
        }

        public int getScore() {
            return score;
        }
    }

    public static class Storage extends Component {
        private String type;
        private int capacityGB;
        private int speedMBps;

        public Storage(String id, String name, double price, String type,
                int capacityGB, int speedMBps) {
            super(id, name, price);
            this.type = type;
            this.capacityGB = capacityGB;
            this.speedMBps = speedMBps;
        }

        public String getType() {
            return type;
        }

        public int getCapacityGB() {
            return capacityGB;
        }

        public int getSpeedMBps() {
            return speedMBps;
        }
    }

    public static class PSU extends Component {
        private int wattage;
        private String formFactor;
        private String efficiency;

        public PSU(String id, String name, double price, int wattage,
                String formFactor, String efficiency) {
            super(id, name, price);
            this.wattage = wattage;
            this.formFactor = formFactor;
            this.efficiency = efficiency;
        }

        public int getWattage() {
            return wattage;
        }

        public String getFormFactor() {
            return formFactor;
        }

        public String getEfficiency() {
            return efficiency;
        }
    }

    public static class Case extends Component {
        private String formFactor;
        private int gpuClearanceMm;
        private int cpuCoolerClearanceMm;
        private String psuFormFactor;

        public Case(String id, String name, double price, String formFactor,
                int gpuClearanceMm, int cpuCoolerClearanceMm,
                String psuFormFactor) {
            super(id, name, price);
            this.formFactor = formFactor;
            this.gpuClearanceMm = gpuClearanceMm;
            this.cpuCoolerClearanceMm = cpuCoolerClearanceMm;
            this.psuFormFactor = psuFormFactor;
        }

        public String getFormFactor() {
            return formFactor;
        }

        public int getGpuClearanceMm() {
            return gpuClearanceMm;
        }

        public int getCpuCoolerClearanceMm() {
            return cpuCoolerClearanceMm;
        }

        public String getPsuFormFactor() {
            return psuFormFactor;
        }
    }

    public static class Cooler extends Component {
        private String type;
        private int heightMm;
        private int maxTdp;
        private String[] compatibleSockets;

        public Cooler(String id, String name, double price, String type,
                int heightMm, int maxTdp, String... compatibleSockets) {
            super(id, name, price);
            this.type = type;
            this.heightMm = heightMm;
            this.maxTdp = maxTdp;
            this.compatibleSockets = compatibleSockets;
        }

        public String getType() {
            return type;
        }

        public int getHeightMm() {
            return heightMm;
        }

        public int getMaxTdp() {
            return maxTdp;
        }

        public String[] getCompatibleSockets() {
            return compatibleSockets;
        }
    }

    // ==================== Sample Data ====================

    private static final List<CPU> ALL_CPUS = Arrays.asList(
            new CPU("cpu1", "Intel Core i9-14900K", 589, "LGA1700", 125, 100),
            new CPU("cpu2", "Intel Core i7-14700K", 419, "LGA1700", 125, 85),
            new CPU("cpu3", "Intel Core i5-14600K", 319, "LGA1700", 125, 70),
            new CPU("cpu4", "AMD Ryzen 9 7950X", 549, "AM5", 170, 98),
            new CPU("cpu5", "AMD Ryzen 7 7700X", 349, "AM5", 105, 80),
            new CPU("cpu6", "AMD Ryzen 5 7600X", 229, "AM5", 105, 65),
            new CPU("cpu0", "None", 0, "None", 0, 0));

    private static final List<Motherboard> ALL_MOTHERBOARDS = Arrays.asList(
            new Motherboard("mb1", "ASUS ROG Z790", 389, "LGA1700", "DDR5",
                    7200, 128, "ATX", 4, 6),
            new Motherboard("mb2", "MSI MPG Z790", 299, "LGA1700", "DDR5", 6400,
                    128, "ATX", 3, 6),
            new Motherboard("mb3", "ASUS TUF B760", 189, "LGA1700", "DDR5",
                    5600, 128, "ATX", 2, 4),
            new Motherboard("mb4", "ASUS ROG X670E", 429, "AM5", "DDR5", 6400,
                    128, "ATX", 4, 6),
            new Motherboard("mb5", "MSI MPG X670", 319, "AM5", "DDR5", 6000,
                    128, "ATX", 3, 6),
            new Motherboard("mb6", "ASUS TUF B650", 199, "AM5", "DDR5", 5600,
                    128, "ATX", 2, 4),
            new Motherboard("mb0", "None", 0, "None", "None", 0, 0, "None", 0,
                    0));

    private static final List<RAM> ALL_RAM = Arrays.asList(
            new RAM("ram1", "32GB DDR5-6000 (2x16GB)", 129, "DDR5", 6000, 32),
            new RAM("ram2", "32GB DDR5-5600 (2x16GB)", 99, "DDR5", 5600, 32),
            new RAM("ram3", "64GB DDR5-6000 (2x32GB)", 219, "DDR5", 6000, 64),
            new RAM("ram4", "16GB DDR5-5600 (2x8GB)", 59, "DDR5", 5600, 16),
            new RAM("ram5", "32GB DDR4-3200 (2x16GB)", 79, "DDR4", 3200, 32),
            new RAM("ram6", "16GB DDR4-3200 (2x8GB)", 45, "DDR4", 3200, 16),
            new RAM("ram0", "None", 0, "None", 0, 0));

    private static final List<GPU> ALL_GPUS = Arrays.asList(
            new GPU("gpu1", "NVIDIA RTX 4090", 1599, 450, 336, 100),
            new GPU("gpu2", "NVIDIA RTX 4080", 1199, 320, 310, 88),
            new GPU("gpu3", "NVIDIA RTX 4070 Ti", 799, 285, 285, 75),
            new GPU("gpu4", "AMD RX 7900 XTX", 999, 355, 320, 85),
            new GPU("gpu5", "AMD RX 7900 XT", 849, 315, 305, 78),
            new GPU("gpu6", "NVIDIA RTX 4060 Ti", 499, 160, 244, 55),
            new GPU("gpu0", "None", 0, 0, 0, 0));

    private static final List<Storage> ALL_STORAGE = Arrays.asList(
            new Storage("stor1", "2TB Samsung 990 Pro (NVMe)", 189, "M.2 NVMe",
                    2000, 7450),
            new Storage("stor2", "1TB Samsung 990 Pro (NVMe)", 109, "M.2 NVMe",
                    1000, 7450),
            new Storage("stor3", "2TB Samsung 870 EVO (SATA SSD)", 149,
                    "2.5\" SATA", 2000, 560),
            new Storage("stor4", "1TB Samsung 870 EVO (SATA SSD)", 89,
                    "2.5\" SATA", 1000, 560),
            new Storage("stor5", "4TB Seagate Barracuda (HDD)", 89,
                    "3.5\" SATA", 4000, 190),
            new Storage("stor6", "2TB WD Blue (HDD)", 54, "3.5\" SATA", 2000,
                    180),
            new Storage("stor7", "None", 0, "None", 0, 0));

    private static final List<PSU> ALL_PSUS = Arrays.asList(
            new PSU("psu1", "1000W Corsair RM1000x (80+ Gold)", 199, 1000,
                    "ATX", "80+ Gold"),
            new PSU("psu2", "850W Corsair RM850x (80+ Gold)", 149, 850, "ATX",
                    "80+ Gold"),
            new PSU("psu3", "750W Corsair RM750x (80+ Gold)", 119, 750, "ATX",
                    "80+ Gold"),
            new PSU("psu4", "650W Corsair RM650x (80+ Gold)", 99, 650, "ATX",
                    "80+ Gold"),
            new PSU("psu5", "550W Corsair RM550x (80+ Gold)", 79, 550, "ATX",
                    "80+ Gold"),
            new PSU("psu0", "None", 0, 0, "None", "None"));

    private static final List<Case> ALL_CASES = Arrays.asList(
            new Case("case1", "NZXT H7 Flow (ATX)", 129, "ATX", 400, 185,
                    "ATX"),
            new Case("case2", "Fractal Design North (ATX)", 139, "ATX", 360,
                    170, "ATX"),
            new Case("case3", "Lian Li O11 Dynamic (ATX)", 149, "ATX", 420, 167,
                    "ATX"),
            new Case("case4", "Corsair 4000D Airflow (ATX)", 104, "ATX", 360,
                    170, "ATX"),
            new Case("case0", "None", 0, "None", 0, 0, "None"));

    private static final List<Cooler> ALL_COOLERS = Arrays.asList(
            new Cooler("cool1", "Noctua NH-D15 (Air)", 109, "Air", 165, 180,
                    "LGA1700", "AM5"),
            new Cooler("cool2", "be quiet! Dark Rock Pro 4 (Air)", 89, "Air",
                    162, 250, "LGA1700", "AM5"),
            new Cooler("cool3", "Arctic Liquid Freezer II 360 (AIO)", 149,
                    "AIO", 60, 350, "LGA1700", "AM5"),
            new Cooler("cool4", "NZXT Kraken X63 (AIO)", 139, "AIO", 58, 280,
                    "LGA1700", "AM5"),
            new Cooler("cool5", "Cooler Master Hyper 212 (Air)", 49, "Air", 158,
                    150, "LGA1700", "AM5"),
            new Cooler("cool0", "None", 0, "None", 0, 0, "None", "None"));

    // ==================== Signal Declarations (~70 total) ====================

    // 1. Component Selection Signals (12)
    private final WritableSignal<CPU> cpuSignal = new ValueSignal<>(
            ALL_CPUS.get(ALL_CPUS.size() - 1)); // None
    private final WritableSignal<Motherboard> motherboardSignal = new ValueSignal<>(
            ALL_MOTHERBOARDS.get(ALL_MOTHERBOARDS.size() - 1)); // None
    private final WritableSignal<RAM> ramSignal = new ValueSignal<>(
            ALL_RAM.get(ALL_RAM.size() - 1)); // None
    private final WritableSignal<GPU> gpuSignal = new ValueSignal<>(
            ALL_GPUS.get(ALL_GPUS.size() - 1)); // None
    private final WritableSignal<Storage> storage1Signal = new ValueSignal<>(
            ALL_STORAGE.get(ALL_STORAGE.size() - 1)); // None
    private final WritableSignal<Storage> storage2Signal = new ValueSignal<>(
            ALL_STORAGE.get(ALL_STORAGE.size() - 1)); // None
    private final WritableSignal<Storage> storage3Signal = new ValueSignal<>(
            ALL_STORAGE.get(ALL_STORAGE.size() - 1)); // None
    private final WritableSignal<PSU> psuSignal = new ValueSignal<>(
            ALL_PSUS.get(ALL_PSUS.size() - 1)); // None
    private final WritableSignal<Case> caseSignal = new ValueSignal<>(
            ALL_CASES.get(ALL_CASES.size() - 1)); // None
    private final WritableSignal<Cooler> coolerSignal = new ValueSignal<>(
            ALL_COOLERS.get(ALL_COOLERS.size() - 1)); // None

    // 2. Computed Price Signals (10)
    private Signal<Double> cpuPriceSignal;
    private Signal<Double> motherboardPriceSignal;
    private Signal<Double> ramPriceSignal;
    private Signal<Double> gpuPriceSignal;
    private Signal<Double> storage1PriceSignal;
    private Signal<Double> storage2PriceSignal;
    private Signal<Double> storage3PriceSignal;
    private Signal<Double> psuPriceSignal;
    private Signal<Double> casePriceSignal;
    private Signal<Double> coolerPriceSignal;
    private Signal<Double> totalPriceSignal;

    // 3. Computed Power Signals (6)
    private Signal<Integer> cpuPowerSignal;
    private Signal<Integer> gpuPowerSignal;
    private Signal<Integer> totalPowerSignal;
    private Signal<Integer> recommendedPsuWattageSignal;
    private Signal<Boolean> psuSufficiencySignal;
    private Signal<Integer> powerMarginSignal;

    // 4. Compatibility Check Signals (15)
    private Signal<Boolean> cpuSocketMatchSignal;
    private Signal<Boolean> ramTypeMatchSignal;
    private Signal<Boolean> ramSpeedSupportedSignal;
    private Signal<Boolean> gpuFitsCaseSignal;
    private Signal<Boolean> coolerFitsCaseSignal;
    private Signal<Boolean> motherboardFitsCaseSignal;
    private Signal<Boolean> m2SlotsAvailableSignal;
    private Signal<Boolean> sataSlotsAvailableSignal;
    private Signal<Boolean> psuFitsCaseSignal;
    private Signal<Boolean> cpuCoolerCompatibleSignal;
    private Signal<Boolean> ramCapacitySupportedSignal;
    private Signal<Boolean> coolerTdpSufficientSignal;
    private Signal<Boolean> allCompatibleSignal;
    private Signal<Boolean> hasCriticalIssuesSignal;
    private Signal<Integer> compatibilityCheckCountSignal;

    // 5. Performance Estimate Signals (8)
    private Signal<Integer> cpuScoreSignal;
    private Signal<Integer> gpuScoreSignal;
    private Signal<Integer> storageSpeedSignal;
    private Signal<Integer> overallScoreSignal;
    private Signal<Integer> gamingScoreSignal;
    private Signal<Integer> productivityScoreSignal;
    private Signal<String> bottleneckSignal;
    private Signal<String> performanceRatingSignal;

    // 6. Validation & Warning Signals (5)
    private Signal<List<String>> missingComponentsSignal;
    private Signal<Integer> warningCountSignal;
    private Signal<Boolean> canBuildSignal;
    private Signal<List<String>> warningMessagesSignal;
    private Signal<Boolean> isValidConfigurationSignal;

    // 7. UI State Signals (4)
    private final WritableSignal<Boolean> showCompatibilityDetailsSignal = new ValueSignal<>(
            true);
    private final WritableSignal<Boolean> showPerformanceDetailsSignal = new ValueSignal<>(
            true);

    public UseCase17View() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        // Initialize all computed signals
        initializePriceSignals();
        initializePowerSignals();
        initializeCompatibilitySignals();
        initializePerformanceSignals();
        initializeValidationSignals();

        // Build UI
        buildLayout();
    }

    // ==================== Signal Initialization ====================

    private void initializePriceSignals() {
        cpuPriceSignal = cpuSignal
                .map(cpu -> cpu != null ? cpu.getPrice() : 0.0);
        motherboardPriceSignal = motherboardSignal
                .map(mb -> mb != null ? mb.getPrice() : 0.0);
        ramPriceSignal = ramSignal
                .map(ram -> ram != null ? ram.getPrice() : 0.0);
        gpuPriceSignal = gpuSignal
                .map(gpu -> gpu != null ? gpu.getPrice() : 0.0);
        storage1PriceSignal = storage1Signal
                .map(s -> s != null ? s.getPrice() : 0.0);
        storage2PriceSignal = storage2Signal
                .map(s -> s != null ? s.getPrice() : 0.0);
        storage3PriceSignal = storage3Signal
                .map(s -> s != null ? s.getPrice() : 0.0);
        psuPriceSignal = psuSignal
                .map(psu -> psu != null ? psu.getPrice() : 0.0);
        casePriceSignal = caseSignal.map(c -> c != null ? c.getPrice() : 0.0);
        coolerPriceSignal = coolerSignal
                .map(cooler -> cooler != null ? cooler.getPrice() : 0.0);

        totalPriceSignal = Signal.computed(() -> cpuPriceSignal.value()
                + motherboardPriceSignal.value() + ramPriceSignal.value()
                + gpuPriceSignal.value() + storage1PriceSignal.value()
                + storage2PriceSignal.value() + storage3PriceSignal.value()
                + psuPriceSignal.value() + casePriceSignal.value()
                + coolerPriceSignal.value());
    }

    private void initializePowerSignals() {
        cpuPowerSignal = cpuSignal.map(cpu -> cpu != null ? cpu.getTdp() : 0);
        gpuPowerSignal = gpuSignal
                .map(gpu -> gpu != null ? gpu.getPowerConsumption() : 0);

        totalPowerSignal = Signal.computed(
                () -> cpuPowerSignal.value() + gpuPowerSignal.value() + 50 // Motherboard,
                                                                           // RAM,
                                                                           // storage,
                                                                           // fans
                                                                           // (estimated)
        );

        recommendedPsuWattageSignal = totalPowerSignal
                .map(power -> (int) (power * 1.3) // 30% headroom
                );

        psuSufficiencySignal = Signal.computed(() -> {
            PSU psu = psuSignal.value();
            if (psu == null)
                return false;
            return psu.getWattage() >= totalPowerSignal.value();
        });

        powerMarginSignal = Signal.computed(() -> {
            PSU psu = psuSignal.value();
            if (psu == null)
                return 0;
            return psu.getWattage() - totalPowerSignal.value();
        });
    }

    private void initializeCompatibilitySignals() {
        cpuSocketMatchSignal = Signal.computed(() -> {
            CPU cpu = cpuSignal.value();
            Motherboard mobo = motherboardSignal.value();
            if (cpu == null || mobo == null || "None".equals(cpu.getName())
                    || "None".equals(mobo.getName()))
                return true;
            return cpu.getSocket().equals(mobo.getSocket());
        });

        ramTypeMatchSignal = Signal.computed(() -> {
            RAM ram = ramSignal.value();
            Motherboard mobo = motherboardSignal.value();
            if (ram == null || mobo == null)
                return true;
            return ram.getType().equals(mobo.getRamType());
        });

        ramSpeedSupportedSignal = Signal.computed(() -> {
            RAM ram = ramSignal.value();
            Motherboard mobo = motherboardSignal.value();
            if (ram == null || mobo == null)
                return true;
            return ram.getSpeed() <= mobo.getMaxRamSpeed();
        });

        ramCapacitySupportedSignal = Signal.computed(() -> {
            RAM ram = ramSignal.value();
            Motherboard mobo = motherboardSignal.value();
            if (ram == null || mobo == null)
                return true;
            return ram.getCapacity() <= mobo.getMaxRamCapacity();
        });

        gpuFitsCaseSignal = Signal.computed(() -> {
            GPU gpu = gpuSignal.value();
            Case pc = caseSignal.value();
            if (gpu == null || pc == null)
                return true;
            return gpu.getLengthMm() <= pc.getGpuClearanceMm();
        });

        coolerFitsCaseSignal = Signal.computed(() -> {
            Cooler cooler = coolerSignal.value();
            Case pc = caseSignal.value();
            if (cooler == null || pc == null)
                return true;
            return cooler.getHeightMm() <= pc.getCpuCoolerClearanceMm();
        });

        motherboardFitsCaseSignal = Signal.computed(() -> {
            Motherboard mobo = motherboardSignal.value();
            Case pc = caseSignal.value();
            if (mobo == null || pc == null)
                return true;
            return mobo.getFormFactor().equals(pc.getFormFactor())
                    || pc.getFormFactor().equals("ATX"); // ATX cases fit all
        });

        m2SlotsAvailableSignal = Signal.computed(() -> {
            Motherboard mobo = motherboardSignal.value();
            Storage stor1 = storage1Signal.value();
            if (mobo == null)
                return true;
            int m2Count = 0;
            if (stor1 != null && stor1.getType().contains("NVMe"))
                m2Count++;
            return m2Count <= mobo.getM2Slots();
        });

        sataSlotsAvailableSignal = Signal.computed(() -> {
            Motherboard mobo = motherboardSignal.value();
            if (mobo == null)
                return true;
            Storage stor2 = storage2Signal.value();
            Storage stor3 = storage3Signal.value();
            int sataCount = 0;
            if (stor2 != null && stor2.getType().contains("SATA"))
                sataCount++;
            if (stor3 != null && stor3.getType().contains("SATA"))
                sataCount++;
            return sataCount <= mobo.getSataSlots();
        });

        psuFitsCaseSignal = Signal.computed(() -> {
            PSU psu = psuSignal.value();
            Case pc = caseSignal.value();
            if (psu == null || pc == null)
                return true;
            return psu.getFormFactor().equals(pc.getPsuFormFactor());
        });

        cpuCoolerCompatibleSignal = Signal.computed(() -> {
            CPU cpu = cpuSignal.value();
            Cooler cooler = coolerSignal.value();
            if (cpu == null || cooler == null)
                return true;
            return Arrays.asList(cooler.getCompatibleSockets())
                    .contains(cpu.getSocket());
        });

        coolerTdpSufficientSignal = Signal.computed(() -> {
            CPU cpu = cpuSignal.value();
            Cooler cooler = coolerSignal.value();
            if (cpu == null || cooler == null)
                return true;
            return cooler.getMaxTdp() >= cpu.getTdp();
        });

        allCompatibleSignal = Signal.computed(() -> cpuSocketMatchSignal.value()
                && ramTypeMatchSignal.value() && ramSpeedSupportedSignal.value()
                && ramCapacitySupportedSignal.value()
                && gpuFitsCaseSignal.value() && coolerFitsCaseSignal.value()
                && motherboardFitsCaseSignal.value()
                && m2SlotsAvailableSignal.value()
                && sataSlotsAvailableSignal.value() && psuFitsCaseSignal.value()
                && cpuCoolerCompatibleSignal.value()
                && coolerTdpSufficientSignal.value()
                && psuSufficiencySignal.value());

        hasCriticalIssuesSignal = allCompatibleSignal
                .map(compatible -> !compatible);

        compatibilityCheckCountSignal = Signal.computed(() -> {
            int passed = 0;
            if (cpuSocketMatchSignal.value())
                passed++;
            if (ramTypeMatchSignal.value())
                passed++;
            if (ramSpeedSupportedSignal.value())
                passed++;
            if (gpuFitsCaseSignal.value())
                passed++;
            if (coolerFitsCaseSignal.value())
                passed++;
            if (motherboardFitsCaseSignal.value())
                passed++;
            if (m2SlotsAvailableSignal.value())
                passed++;
            if (sataSlotsAvailableSignal.value())
                passed++;
            if (psuFitsCaseSignal.value())
                passed++;
            if (cpuCoolerCompatibleSignal.value())
                passed++;
            if (coolerTdpSufficientSignal.value())
                passed++;
            if (ramCapacitySupportedSignal.value())
                passed++;
            if (psuSufficiencySignal.value())
                passed++;
            return passed;
        });
    }

    private void initializePerformanceSignals() {
        cpuScoreSignal = cpuSignal.map(cpu -> cpu != null ? cpu.getScore() : 0);
        gpuScoreSignal = gpuSignal.map(gpu -> gpu != null ? gpu.getScore() : 0);
        storageSpeedSignal = storage1Signal
                .map(s -> s != null ? s.getSpeedMBps() : 0);

        overallScoreSignal = Signal
                .computed(() -> (int) (cpuScoreSignal.value() * 0.3
                        + gpuScoreSignal.value() * 0.6
                        + storageSpeedSignal.value() / 100.0 * 0.1));

        gamingScoreSignal = Signal
                .computed(() -> (int) (cpuScoreSignal.value() * 0.2
                        + gpuScoreSignal.value() * 0.8));

        productivityScoreSignal = Signal
                .computed(() -> (int) (cpuScoreSignal.value() * 0.6
                        + gpuScoreSignal.value() * 0.3
                        + storageSpeedSignal.value() / 100.0 * 0.1));

        bottleneckSignal = Signal.computed(() -> {
            int cpuScore = cpuScoreSignal.value();
            int gpuScore = gpuScoreSignal.value();
            if (cpuScore == 0 || gpuScore == 0)
                return "N/A";
            if (cpuScore < gpuScore * 0.6)
                return "CPU";
            if (gpuScore < cpuScore * 0.6)
                return "GPU";
            return "Balanced";
        });

        performanceRatingSignal = overallScoreSignal.map(score -> {
            if (score >= 85)
                return "Enthusiast";
            if (score >= 70)
                return "High-End";
            if (score >= 50)
                return "Mid-Range";
            if (score >= 30)
                return "Entry-Level";
            return "Budget";
        });
    }

    private void initializeValidationSignals() {
        missingComponentsSignal = Signal.computed(() -> {
            List<String> missing = new java.util.ArrayList<>();
            if (cpuSignal.value() == null)
                missing.add("CPU");
            if (motherboardSignal.value() == null)
                missing.add("Motherboard");
            if (ramSignal.value() == null)
                missing.add("RAM");
            if (gpuSignal.value() == null)
                missing.add("GPU");
            if (storage1Signal.value() == null)
                missing.add("Primary Storage");
            if (psuSignal.value() == null)
                missing.add("Power Supply");
            if (caseSignal.value() == null)
                missing.add("Case");
            if (coolerSignal.value() == null)
                missing.add("CPU Cooler");
            return missing;
        });

        warningMessagesSignal = Signal.computed(() -> {
            List<String> warnings = new java.util.ArrayList<>();
            if (!cpuSocketMatchSignal.value())
                warnings.add("⚠ CPU socket doesn't match motherboard");
            if (!ramTypeMatchSignal.value())
                warnings.add("⚠ RAM type doesn't match motherboard");
            if (!ramSpeedSupportedSignal.value())
                warnings.add("⚠ RAM speed not supported by motherboard");
            if (!gpuFitsCaseSignal.value())
                warnings.add("⚠ GPU too long for case");
            if (!coolerFitsCaseSignal.value())
                warnings.add("⚠ CPU cooler too tall for case");
            if (!psuSufficiencySignal.value() && psuSignal.value() != null)
                warnings.add("⚠ PSU wattage insufficient");
            if (!cpuCoolerCompatibleSignal.value())
                warnings.add("⚠ Cooler not compatible with CPU socket");
            if (!coolerTdpSufficientSignal.value())
                warnings.add("⚠ Cooler TDP rating insufficient for CPU");
            return warnings;
        });

        warningCountSignal = warningMessagesSignal.map(List::size);

        canBuildSignal = Signal
                .computed(() -> missingComponentsSignal.value().isEmpty()
                        && allCompatibleSignal.value());

        isValidConfigurationSignal = canBuildSignal;
    }

    // ==================== UI Layout ====================

    private void buildLayout() {
        H2 title = new H2("Use Case 17: Custom PC Builder (70 Signals)");

        Paragraph description = new Paragraph(
                "This use case demonstrates handling complex state at scale with ~70 interdependent signals. "
                        + "Select components to build a custom PC. Watch how compatibility checks, price calculations, "
                        + "power requirements, and performance estimates all update reactively as you make selections.");

        // Signal count display
        Div signalCountBox = new Div();
        signalCountBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "0.75em").set("border-radius", "4px")
                .set("margin-bottom", "1em").set("font-size", "0.9em");
        signalCountBox.add(new Span(
                "📊 Active Signals: 12 component selections + 40+ computed values + 15 compatibility checks + 8 performance metrics = ~70 total signals"));

        // Main content: 3-column layout
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);

        // Left column: Component selection
        VerticalLayout selectionColumn = buildSelectionColumn();
        selectionColumn.setWidth("400px");

        // Center column: Build summary
        VerticalLayout summaryColumn = buildSummaryColumn();
        summaryColumn.setWidth("300px");

        // Right column: Statistics
        VerticalLayout statsColumn = buildStatsColumn();
        statsColumn.setWidth("350px");

        mainLayout.add(selectionColumn, summaryColumn, statsColumn);

        // Bottom: Compatibility checks
        Div compatibilitySection = buildCompatibilitySection();

        // Actions
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button resetButton = new Button("Reset Build", event -> resetBuild());
        resetButton.addThemeName("tertiary");

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addToCartButton.bindEnabled(canBuildSignal);

        Signal<String> cartButtonText = Signal.computed(() -> {
            if (canBuildSignal.value()) {
                return "Add to Cart ($"
                        + String.format("%.0f", totalPriceSignal.value()) + ")";
            }
            return "Complete Build First";
        });
        addToCartButton.bindText(cartButtonText);

        actions.add(resetButton, addToCartButton);

        add(title, description, signalCountBox, mainLayout,
                compatibilitySection, actions);
    }

    private VerticalLayout buildSelectionColumn() {
        VerticalLayout column = new VerticalLayout();
        column.setSpacing(false);
        column.setPadding(false);

        H3 header = new H3("Component Selection");
        header.getStyle().set("margin-top", "0");

        column.add(header);
        column.add(createComponentSelector("CPU", ALL_CPUS, cpuSignal));
        column.add(createComponentSelector("Motherboard", ALL_MOTHERBOARDS,
                motherboardSignal));
        column.add(createComponentSelector("RAM", ALL_RAM, ramSignal));
        column.add(createComponentSelector("GPU", ALL_GPUS, gpuSignal));
        column.add(createComponentSelector("Primary Storage", ALL_STORAGE,
                storage1Signal));
        column.add(createComponentSelector("Secondary Storage", ALL_STORAGE,
                storage2Signal));
        column.add(createComponentSelector("PSU", ALL_PSUS, psuSignal));
        column.add(createComponentSelector("Case", ALL_CASES, caseSignal));
        column.add(createComponentSelector("CPU Cooler", ALL_COOLERS,
                coolerSignal));

        return column;
    }

    private <T extends Component> ComboBox<T> createComponentSelector(
            String label, List<T> items, WritableSignal<T> signal) {
        ComboBox<T> combo = new ComboBox<>(label);
        combo.setItems(items);
        combo.setItemLabelGenerator(Component::getName);
        combo.setWidthFull();
        combo.bindValue(signal);
        return combo;
    }

    private VerticalLayout buildSummaryColumn() {
        VerticalLayout column = new VerticalLayout();
        column.setSpacing(false);
        column.setPadding(false);

        H3 header = new H3("Build Summary");
        header.getStyle().set("margin-top", "0");

        Div summary = new Div();
        summary.getStyle().set("background-color", "#f5f5f5")
                .set("padding", "1em").set("border-radius", "4px")
                .set("font-size", "0.9em");

        Signal<List<Component>> selectedComponentsSignal = Signal
                .computed(() -> {
                    List<Component> selected = new java.util.ArrayList<>();
                    if (cpuSignal.value() != null)
                        selected.add(cpuSignal.value());
                    if (motherboardSignal.value() != null)
                        selected.add(motherboardSignal.value());
                    if (ramSignal.value() != null)
                        selected.add(ramSignal.value());
                    if (gpuSignal.value() != null)
                        selected.add(gpuSignal.value());
                    if (storage1Signal.value() != null)
                        selected.add(storage1Signal.value());
                    if (storage2Signal.value() != null
                            && storage2Signal.value().getPrice() > 0)
                        selected.add(storage2Signal.value());
                    if (psuSignal.value() != null)
                        selected.add(psuSignal.value());
                    if (caseSignal.value() != null)
                        selected.add(caseSignal.value());
                    if (coolerSignal.value() != null)
                        selected.add(coolerSignal.value());
                    return selected;
                });

        MissingAPI.bindComponentChildren(summary, selectedComponentsSignal,
                comp -> {
                    Div item = new Div();
                    item.getStyle().set("margin-bottom", "0.5em")
                            .set("display", "flex")
                            .set("justify-content", "space-between");

                    Span name = new Span(comp.getName());
                    name.getStyle().set("flex", "1");

                    Span price = new Span(
                            "$" + String.format("%.0f", comp.getPrice()));
                    price.getStyle().set("font-weight", "bold").set("color",
                            "var(--lumo-primary-color)");

                    item.add(name, price);
                    return item;
                });

        column.add(header, summary);
        return column;
    }

    private VerticalLayout buildStatsColumn() {
        VerticalLayout column = new VerticalLayout();
        column.setSpacing(true);
        column.setPadding(false);

        H3 header = new H3("Statistics");
        header.getStyle().set("margin-top", "0");

        // Price box
        Div priceBox = createStatBox("Total Price",
                totalPriceSignal.map(p -> "$" + String.format("%.0f", p)),
                "#e8f5e9");

        // Power box
        Div powerBox = new Div();
        powerBox.getStyle().set("background-color", "#fff3e0")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-bottom", "0.5em");

        Span powerLabel = new Span("Power Consumption");
        powerLabel.getStyle().set("display", "block").set("font-weight", "bold")
                .set("margin-bottom", "0.5em");

        Span powerValue = new Span(totalPowerSignal.map(p -> p + "W total"));
        powerValue.getStyle().set("display", "block");

        Signal<String> psuStatusText = Signal.computed(() -> {
            PSU psu = psuSignal.value();
            if (psu == null)
                return "No PSU selected";
            boolean sufficient = psuSufficiencySignal.value();
            int margin = powerMarginSignal.value();
            return psu.getWattage() + "W PSU: "
                    + (sufficient ? "✓ OK (+" + margin + "W)"
                            : "⚠ Insufficient");
        });
        Span psuStatus = new Span(psuStatusText);
        psuStatus.getStyle().set("display", "block");

        powerBox.add(powerLabel, powerValue, psuStatus);

        // Compatibility box
        Div compatBox = createStatBox("Compatibility",
                compatibilityCheckCountSignal
                        .map(count -> count + "/13 checks passing"),
                allCompatibleSignal.map(ok -> ok ? "#e8f5e9" : "#ffebee"));

        // Performance box
        Div perfBox = new Div();
        perfBox.getStyle().set("background-color", "#e3f2fd")
                .set("padding", "1em").set("border-radius", "4px");

        Span perfLabel = new Span("Performance");
        perfLabel.getStyle().set("display", "block").set("font-weight", "bold")
                .set("margin-bottom", "0.5em");

        Span perfRating = new Span(performanceRatingSignal);
        perfRating.getStyle().set("display", "block").set("font-size", "1.2em")
                .set("color", "var(--lumo-primary-color)");

        Span perfGaming = new Span(gamingScoreSignal.map(s -> "Gaming: " + s + "/100"));
        perfGaming.getStyle().set("display", "block").set("font-size", "0.9em");

        Span perfBottleneck = new Span(bottleneckSignal.map(b -> "Bottleneck: " + b));
        perfBottleneck.getStyle().set("display", "block").set("font-size",
                "0.9em");

        perfBox.add(perfLabel, perfRating, perfGaming, perfBottleneck);

        column.add(header, priceBox, powerBox, compatBox, perfBox);
        return column;
    }

    private Div createStatBox(String label, Signal<String> valueSignal,
            String bgColor) {
        Div box = new Div();
        box.getStyle().set("background-color", bgColor).set("padding", "1em")
                .set("border-radius", "4px").set("margin-bottom", "0.5em");

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("display", "block").set("font-weight", "bold")
                .set("margin-bottom", "0.5em");

        Span valueSpan = new Span(valueSignal);
        valueSpan.getStyle().set("display", "block").set("font-size", "1.2em");

        box.add(labelSpan, valueSpan);
        return box;
    }

    private Div createStatBox(String label, Signal<String> valueSignal,
            Signal<String> bgColorSignal) {
        Div box = new Div();
        box.getStyle().bind("background-color", bgColorSignal);
        box.getStyle().set("padding", "1em").set("border-radius", "4px")
                .set("margin-bottom", "0.5em");

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("display", "block").set("font-weight", "bold")
                .set("margin-bottom", "0.5em");

        Span valueSpan = new Span(valueSignal);
        valueSpan.getStyle().set("display", "block").set("font-size", "1.2em");

        box.add(labelSpan, valueSpan);
        return box;
    }

    private Div buildCompatibilitySection() {
        Div section = new Div();
        section.getStyle().set("background-color", "#f5f5f5")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em");

        H3 header = new H3("Compatibility Checks");
        header.getStyle().set("margin-top", "0");

        Div checksContainer = new Div();

        Signal<List<String>> compatibilityStatusSignal = Signal.computed(() -> {
            List<String> statuses = new java.util.ArrayList<>();
            statuses.add(formatCheck("CPU socket matches motherboard",
                    cpuSocketMatchSignal.value()));
            statuses.add(formatCheck("RAM type matches motherboard",
                    ramTypeMatchSignal.value()));
            statuses.add(formatCheck("RAM speed supported",
                    ramSpeedSupportedSignal.value()));
            statuses.add(
                    formatCheck("GPU fits in case", gpuFitsCaseSignal.value()));
            statuses.add(formatCheck("CPU cooler fits in case",
                    coolerFitsCaseSignal.value()));
            statuses.add(formatCheck("Motherboard fits in case",
                    motherboardFitsCaseSignal.value()));
            statuses.add(formatCheck("M.2 slots available",
                    m2SlotsAvailableSignal.value()));
            statuses.add(formatCheck("SATA ports available",
                    sataSlotsAvailableSignal.value()));
            statuses.add(
                    formatCheck("PSU fits in case", psuFitsCaseSignal.value()));
            statuses.add(formatCheck("Cooler compatible with CPU",
                    cpuCoolerCompatibleSignal.value()));
            statuses.add(formatCheck("Cooler TDP sufficient",
                    coolerTdpSufficientSignal.value()));
            statuses.add(formatCheck("RAM capacity supported",
                    ramCapacitySupportedSignal.value()));
            statuses.add(formatCheck("PSU wattage sufficient",
                    psuSufficiencySignal.value()));
            return statuses;
        });

        MissingAPI.bindComponentChildren(checksContainer,
                compatibilityStatusSignal, status -> {
                    Div checkDiv = new Div();
                    checkDiv.getStyle().set("padding", "0.25em 0")
                            .set("font-size", "0.9em");
                    checkDiv.getElement().setProperty("innerHTML", status);
                    return checkDiv;
                });

        section.add(header, checksContainer);
        return section;
    }

    private String formatCheck(String label, boolean passes) {
        String icon = passes ? "✓" : "✗";
        String color = passes ? "var(--lumo-success-color)"
                : "var(--lumo-error-color)";
        return "<span style='color: " + color + "; font-weight: bold;'>" + icon
                + "</span> " + label;
    }

    private void resetBuild() {
        cpuSignal.value(ALL_CPUS.get(ALL_CPUS.size() - 1)); // None
        motherboardSignal
                .value(ALL_MOTHERBOARDS.get(ALL_MOTHERBOARDS.size() - 1)); // None
        ramSignal.value(ALL_RAM.get(ALL_RAM.size() - 1)); // None
        gpuSignal.value(ALL_GPUS.get(ALL_GPUS.size() - 1)); // None
        storage1Signal.value(ALL_STORAGE.get(ALL_STORAGE.size() - 1)); // None
        storage2Signal.value(ALL_STORAGE.get(ALL_STORAGE.size() - 1)); // None
        storage3Signal.value(ALL_STORAGE.get(ALL_STORAGE.size() - 1)); // None
        psuSignal.value(ALL_PSUS.get(ALL_PSUS.size() - 1)); // None
        caseSignal.value(ALL_CASES.get(ALL_CASES.size() - 1)); // None
        coolerSignal.value(ALL_COOLERS.get(ALL_COOLERS.size() - 1)); // None
    }
}
