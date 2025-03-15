import matplotlib.pyplot as plt
import numpy as np

# 读取数据
file_path = "allreward.txt"
ddqnlstm_rewards = []
ddqn_rewards = []

with open(file_path, "r") as file:
    for line in file:
        values = line.strip().split("\t")  # 解析 Tab 分隔的数据
        if len(values) == 2:
            ddqnlstm_rewards.append(float(values[0]))
            ddqn_rewards.append(float(values[1]))

# 生成 x 轴数据（假设数据从 1 开始）
epochs = np.arange(1, len(ddqnlstm_rewards) + 1)

# 绘制图像
plt.figure(figsize=(10, 6))
plt.plot(epochs, ddqnlstm_rewards, label="Improved_ddqn",
         linestyle='-', markersize=4)
plt.plot(epochs, ddqn_rewards, label="ddqn",
         linestyle='--', markersize=4)

# 添加标签和标题
plt.xlabel("Epoch")
plt.ylabel("Reward")
plt.title("DDQNLSTM vs. DDQN Reward Comparison")
plt.legend()
plt.grid(True)
plt.savefig("reward_comparison.png")
# 显示图像
plt.show()
