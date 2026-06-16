package com.ertores.block;

import com.ertores.block.entity.ProcessingMachineBlockEntity;
import com.ertores.processing.MachineOperation;
import com.ertores.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ProcessingMachineBlock extends BaseEntityBlock {
	public static final MapCodec<ProcessingMachineBlock> CODEC = simpleCodec(properties -> new ProcessingMachineBlock(MachineOperation.CRUSHING, properties));
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	private final MachineOperation operation;

	public ProcessingMachineBlock(MachineOperation operation, Properties properties) {
		super(properties);
		this.operation = operation;
		registerDefaultState(defaultBlockState().setValue(ACTIVE, false).setValue(FACING, Direction.NORTH));
	}

	public MachineOperation operation() {
		return operation;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ProcessingMachineBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE, FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState()
				.setValue(ACTIVE, false)
				.setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (stack.isEmpty()) {
			return interactWithEmptyHand(level, pos, player);
		}

		if (level.getBlockEntity(pos) instanceof ProcessingMachineBlockEntity machine && machine.insertOne(stack, player)) {
			player.sendOverlayMessage(Component.translatable("message.ertores.machine_inserted"));
			return InteractionResult.SUCCESS_SERVER;
		}

		player.sendOverlayMessage(Component.translatable("message.ertores.machine_reject"));
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		return interactWithEmptyHand(level, pos, player);
	}

	private InteractionResult interactWithEmptyHand(Level level, BlockPos pos, Player player) {
		if (level.getBlockEntity(pos) instanceof ProcessingMachineBlockEntity machine) {
			if (!machine.extractTo(player)) {
				player.sendOverlayMessage(machine.status());
			}
			return InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.PASS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ProcessingMachineBlockEntity machine) {
			machine.dropStoredItems();
		}

		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}

		return createTickerHelper(type, ModBlockEntities.PROCESSING_MACHINE, ProcessingMachineBlockEntity::tick);
	}
}
