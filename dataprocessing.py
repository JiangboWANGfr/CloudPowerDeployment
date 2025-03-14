import numpy as np
import matplotlib
import matplotlib.pyplot as plt
# 设置全局字体为 SimHei（黑体），适用于 Windows 和部分 Linux 发行版
matplotlib.rcParams['font.sans-serif'] = ['SimHei']  # 黑体
matplotlib.rcParams['axes.unicode_minus'] = False   # 解决负号 '-' 显示问题

def read_data(filename):
    """
    读取数据文件，每行格式：
    虚拟机数量 Q-Learning DDQN Greedy PSO
    """
    data = np.loadtxt(filename, dtype=str)
    x_labels = data[:, 0]  # 提取虚拟机数量
    y_values = data[:, 1:].astype(float)  # 提取算法数据（转为 float）
    return x_labels, y_values


# 读取数据
x_labels, power_data = read_data("./allpower.txt")
_, slav_data = read_data("./allslav.txt")
_, balance_data = read_data("./allbalance.txt")

# 定义算法标签
algorithms = ["DDQN-LSTM","DDQN" ,"Q-Learning", "Greedy", "PSO"]
colors = ["#ED7C31", "#FFBF00", "#70AC46", "#9E480E", "#997200"]
x = np.arange(len(x_labels))  # X轴索引

# 绘制柱状图

def plot_bar_chart(y_data, title, filename):
    plt.figure(figsize=(10, 6))
    width = 0.15  # 柱状图宽度
    for i, alg in enumerate(algorithms):
        plt.bar(x + i * width, y_data[:, i],
                width=width, label=alg, color=colors[i])

    plt.xlabel("虚拟机数量")
    plt.ylabel(title)
    # plt.title(f"各类算法随虚拟机数量的{title}变化")
    plt.xticks(x + width, x_labels)
    plt.legend()
    plt.grid(axis="y", linestyle="--", alpha=0.7)
    plt.legend(loc="upper center", bbox_to_anchor=(
        0.5, -0.08), ncol=5, frameon=False)


    # 保存图像
    plt.savefig(filename)
    plt.show()


# 生成所有图表
plot_bar_chart(power_data, "能耗(kWh)", "power_comparison.png")
plot_bar_chart(slav_data, "SLAV(%)", "slav_comparison.png")
plot_bar_chart(balance_data, "负载均衡度", "balance_comparison.png")
