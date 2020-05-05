package edu.iis.mto.testreactor.washingmachine;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WashingMachineTest {
    @Mock
    private DirtDetector dirtDetector;
    @Mock
    private Engine engine;
    @Mock
    private WaterPump waterPump;
    private WashingMachine washingMashine;

    //input
	private final Material irrelevant = Material.COTTON;
	private final Material relevant = Material.JEANS;

	private final double properWeightKg = 7d;
	private final double improperWeightKg = 10d;
	private final double negativeWeightKg = -2d;
	private final double zeroWeightKg = 0d;

	private final Program staticProgram = Program.LONG;
	private final Program nonstaticProgram = Program.AUTODETECT;


    @BeforeEach
    void setUp() throws Exception {
        washingMashine = new WashingMachine(dirtDetector, engine, waterPump);
    }

    @Test
    void checkIfBatchIsProperAndProgramIsStatic() {
        LaundryBatch laundryBatch = batch(irrelevant, properWeightKg);
        ProgramConfiguration programConfiguration = configWithSpin(staticProgram);

        LaundryStatus status = washingMashine.start(laundryBatch, programConfiguration);
        Assertions.assertEquals(success(staticProgram), status);
    }

    @Test
    void checkIfEngineAndPumpAreCalledWithProperBatchAndStaticProgram() throws EngineException, WaterPumpException {
        LaundryBatch laundryBatch = batch(irrelevant, properWeightKg);
        ProgramConfiguration programConfiguration = configWithSpin(staticProgram);

        washingMashine.start(laundryBatch, programConfiguration);
        InOrder callOrder = Mockito.inOrder(waterPump, engine);
        callOrder.verify(waterPump).pour(properWeightKg);
        callOrder.verify(engine).runWashing(staticProgram.getTimeInMinutes());
        callOrder.verify(waterPump).release();
        callOrder.verify(engine).spin();
    }

    private LaundryBatch batch(Material material, double weight){
        return LaundryBatch.builder().
                withMaterialType(material).
                withWeightKg(weight).
                build();
    }

    private ProgramConfiguration configWithSpin(Program program){
        return ProgramConfiguration.builder().
                withProgram(program).
                withSpin(true).
                build();
    }

    private LaundryStatus success(Program program){
        return LaundryStatus.builder().
                withErrorCode(ErrorCode.NO_ERROR).
                withResult(Result.SUCCESS).
                withRunnedProgram(program).
                build();
    }

    private LaundryStatus error(ErrorCode code, Program program) {
        return LaundryStatus.builder()
                .withResult(Result.FAILURE)
                .withRunnedProgram(program)
                .withErrorCode(code)
                .build();
    }
}
