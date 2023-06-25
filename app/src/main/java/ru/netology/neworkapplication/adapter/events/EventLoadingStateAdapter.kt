package ru.netology.neworkapplication.adapter.events


//class PostLoadingStateAdapter(private val retryListener: () -> Unit) :
//    LoadStateAdapter<PostLoadingViewHolder>() {
//    override fun onBindViewHolder(holder: PostLoadingViewHolder, loadState: LoadState) {
//        holder.bind(loadState)
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        loadState: LoadState
//    ): PostLoadingViewHolder =
//        PostLoadingViewHolder(
//            ItemLoadingBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent, false
//            ),
//            retryListener,
//        )
//
//
//}
//
//class PostLoadingViewHolder(
//    private val itemLoadingBinding: ItemLoadingBinding,
//    private val retryListener: () -> Unit
//) :
//    RecyclerView.ViewHolder(itemLoadingBinding.root) {
//    fun bind(loadState: LoadState) {
//        itemLoadingBinding.apply {
//            progress.isVisible = loadState is LoadState.Loading
//            retryButton.isVisible = loadState is LoadState.Error
//            retryButton.setOnClickListener { retryListener() }
//        }
//    }
//
//}